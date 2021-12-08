package com.hexyoungs.pegasocks

import android.util.Log;
import android.text.TextUtils

import android.os.ParcelFileDescriptor
import java.util.*
import kotlin.collections.ArrayList


class NativeLibs {
  private val TAG = "NativeLibs"

  companion object {
    init {
      System.loadLibrary("native-libs")
    }
  }

  external fun getPegasVersion() : String
  external fun getTun2SocksVersion(): String

  external fun startPegaSocks(configPath: String?, threads: Int): Boolean
  external fun stopPegaSocks()

  private external fun _startTun2Socks(args: Array<String>): Int
  external fun stopTun2Socks()


  fun printNativeLibsVersion() {
    var ver = getPegasVersion();
    Log.v(TAG, "pegasocks version: " + ver);
    ver = getTun2SocksVersion();
    Log.v(TAG, "tun2socks version: " + ver);
  }


  fun startTun2Socks(
    vpnInterfaceFileDescriptor: ParcelFileDescriptor,
    vpnInterfaceMtu: Int,
    socksServerAddress: String?,
    socksServerPort: Int,
    netIPv4Address: String?,
    netIPv6Address: String?,
    netmask: String?,
    forwardUdp: Boolean
  ): Boolean {
    val arguments: ArrayList<String> = ArrayList()
    arguments.add("badvpn-tun2socks")
    arguments.addAll(Arrays.asList("--logger", "stdout"))
    arguments.addAll(Arrays.asList("--loglevel", "info"))
    arguments.addAll(Arrays.asList("--tunfd", vpnInterfaceFileDescriptor.fd.toString()))
    arguments.addAll(Arrays.asList("--tunmtu", vpnInterfaceMtu.toString()))
    arguments.addAll(Arrays.asList("--netif-ipaddr", netIPv4Address))
    if (!TextUtils.isEmpty(netIPv6Address)) {
      arguments.addAll(Arrays.asList("--netif-ip6addr", netIPv6Address))
    }
    arguments.addAll(Arrays.asList("--netif-netmask", netmask))
    arguments.addAll(
      Arrays.asList(
        "--socks-server-addr",
        java.lang.String.format(Locale.US, "%s:%d", socksServerAddress, socksServerPort)
      )
    )
    if (forwardUdp) {
      arguments.add("--socks5-udp")
    }
    val exitCode: Int = _startTun2Socks(arguments.toArray(arrayOf<String>()))
    return exitCode == 0
  }
}
