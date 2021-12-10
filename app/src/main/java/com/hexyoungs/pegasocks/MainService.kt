package com.hexyoungs.pegasocks;

import android.app.Activity
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.net.VpnService;
import android.os.ParcelFileDescriptor

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.os.Build
import androidx.annotation.RequiresApi
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import java.lang.Exception
import android.app.PendingIntent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts

import androidx.core.app.NotificationManagerCompat

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import kotlin.collections.HashSet


class MainService : VpnService() {
    companion object {
        const val ACTION_START = "pegas_start"
        const val ACTION_STOP = "pegas_stop"

        private const val TAG = "vpn_service"
        private const val PRIVATE_VLAN4_CLIENT = "10.0.0.1"
        private const val PRIVATE_VLAN4_ROUTER = "10.0.0.2"

        private const val PRIVATE_VLAN6_CLIENT = "fc00::1"
        private const val PRIVATE_VLAN6_ROUTER = "fc00::2"

        private const val PRIVATE_NETMASK = "255.255.255.252"
        private const val PRIVATE_MTU = 1500

        var isTun2SocksRunning = false
    }

    private var nl: NativeLibs? = null
    private var notification: Notification? = null
    private var connectivityManager: ConnectivityManager? = null
    private var networkCb: NetworkConnectivityCallback? = null

    @Volatile
    private var serverSocket: ServerSocket? = null
    private var descriptor: ParcelFileDescriptor? = null

    private var protectorThread: Thread? = null
    private var pegaSocksThread: Thread? = null
    private var tun2SocksThread: Thread? = null
    private var isNetworkCallbackRegistered = false


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action.equals(ACTION_STOP)) {
            stopService();
            return START_NOT_STICKY;
        }
        startService()
        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()

        // init native libs
        nl = NativeLibs()
        nl!!.printNativeLibsVersion();

        // init notification
        initNotification()

        // init connectivity manager and callback
        initNetworkConnectivity()
    }

    private fun initNotification() {
        val notificationManager = NotificationManagerCompat.from(this)

        val notificationChannel = NotificationChannelCompat.Builder(
            "vpn_service", NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
            .setName("VPN Service")
            .build()
        notificationManager.createNotificationChannel(notificationChannel)

        val stopIntent = Intent(this, MainService::class.java)
        stopIntent.action = ACTION_STOP

        val stopPendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 1, stopIntent, 0)
        } else {
            PendingIntent.getService(this, 1, stopIntent, 0)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this, 2, Intent(
                this,
                MainActivity::class.java
            ), 0
        )

        notification = NotificationCompat.Builder(this, notificationChannel.id)
            .setContentTitle("VPN Service")
            .setContentText("Pegasocks Started")
            .setSmallIcon(R.drawable.ic_notify)
            .setColor(ContextCompat.getColor(this, R.color.pegas_dark))
            .addAction(R.drawable.ic_notify, "Stop", stopPendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
            .setContentIntent(contentPendingIntent)
            .build()
    }

    private fun initNetworkConnectivity() {
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            networkCb = NetworkConnectivityCallback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    override fun onRevoke() {
        stopService()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        stopService()
    }

    private fun startService() {
        if (isTun2SocksRunning) {
            Log.w(TAG, "Already running!?");
            return;
        }

        // notify user
        startForeground(1, notification);

        // create VPN service, only support ipv4 for now
        val builder: Builder = Builder().setSession("Pegas").setMtu(PRIVATE_MTU)
        builder.addAddress(PRIVATE_VLAN4_CLIENT, 30).addRoute("0.0.0.0", 0)

        // init DNS for ipv4
        for (iPv4DNSServer in HashSet(listOf("1.0.0.1", "1.1.1.1"))) {
            // default to cloudflare DNS server
            builder.addDnsServer(iPv4DNSServer)
        }

        // mark as not metered
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false);
        }

        // set active network
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager!!.activeNetwork
            if (activeNetwork != null) {
                builder.setUnderlyingNetworks(arrayOf(activeNetwork))
            }
        }

        // get the interface
        descriptor = builder.establish()
        if (descriptor == null) {
            stopSelf() // !?
            return
        }

        // spawn worker threads
        // protect server -> pegas -> tun2socks
        protectorThread = Thread {
            try {
                serverSocket = ServerSocket(9091)
                val buffer: ByteBuffer = ByteBuffer.allocate(4)
                while (true) {
                    val socket: Socket = serverSocket!!.accept()
                    buffer.clear()
                    val size: Int = socket.getInputStream().read(buffer.array())
                    if (size == 4) {
                        val fd: Int = buffer.int
                        buffer.clear()
                        if (protect(fd)) {
                            buffer.putInt(fd)
                        } else {
                            buffer.putInt(-1)
                        }
                    } else {
                        buffer.putInt(-1)
                    }
                    socket.getOutputStream().write(buffer.array())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        protectorThread!!.start()

        pegaSocksThread = Thread {
            val configPath = getPegasConfigABSPath(this)
            val result: Boolean = nl!!.startPegaSocks(configPath, 1)
            Log.d(TAG, "pegas stopped, result: $result")
        }
        pegaSocksThread!!.start()

        tun2SocksThread = Thread {
            isTun2SocksRunning = true
            val result: Boolean = nl!!.startTun2Socks(
                descriptor!!,
                PRIVATE_MTU,
                "127.0.0.1",
                1080,
                PRIVATE_VLAN4_ROUTER,
                PRIVATE_VLAN6_ROUTER,
                PRIVATE_NETMASK,
                true
            )
            Log.d(TAG, "tun2socks stopped, result: $result")
            isTun2SocksRunning = false
        }
        tun2SocksThread!!.start()


        // register callback for connectivity change
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            registerNetworkCallback()
        }
    }

    private fun stopService() {
        if (!isTun2SocksRunning) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // unregister callback
            unregisterNetworkCallback()
        }

        nl!!.stopTun2Socks()
        try {
            tun2SocksThread!!.join()
            descriptor!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        descriptor = null

        nl!!.stopPegaSocks()
        try {
            pegaSocksThread!!.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        try {
            serverSocket!!.close()
            protectorThread!!.join()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        serverSocket = null

        stopSelf()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    inner class NetworkConnectivityCallback : NetworkCallback() {
        override fun onAvailable(network: Network) {
            setUnderlyingNetworks(arrayOf(network))
        }

        override fun onCapabilitiesChanged(
            network: Network, networkCapabilities: NetworkCapabilities
        ) {
            setUnderlyingNetworks(arrayOf(network))
        }

        override fun onLost(network: Network) {
            setUnderlyingNetworks(null)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun registerNetworkCallback() {
        val builder = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) { // workarounds for OEM bugs
            builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)
        }
        val request = builder.build()

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                networkCb?.let { connectivityManager?.registerNetworkCallback(request, it) }
            } else {
                networkCb?.let { connectivityManager?.requestNetwork(request, it) }
            }
            isNetworkCallbackRegistered = true
        } catch (se: SecurityException) {
            se.printStackTrace()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun unregisterNetworkCallback() {
        try {
            if (isNetworkCallbackRegistered) {
                networkCb?.let { connectivityManager?.unregisterNetworkCallback(it) }
                isNetworkCallbackRegistered = false
            }
        } catch (e: Exception) {
            // Ignore, monitor not installed if the connectivity checks failed.
        }
    }
}
