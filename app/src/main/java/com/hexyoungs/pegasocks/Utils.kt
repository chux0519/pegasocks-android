package com.hexyoungs.pegasocks

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.*
import java.lang.String.copyValueOf
import java.net.*

const val PEGAS_DIR = "pegas"
const val PEGAS_CONFIG_FILE = "pegasrc"
const val PEGAS_ACL_FILE = "default.acl"
const val PEGAS_LOG_FILE = "pegas.log"

fun loadPegasConfig(context: Context): String {
    val path = context.filesDir
    val pegasDir = File(path, PEGAS_DIR)
    if (!pegasDir.exists()) {
        pegasDir.mkdirs()
    }
    val file = File(pegasDir, PEGAS_CONFIG_FILE)
    if (!file.exists()) {
        return ""
    }
    val inputAsString = FileInputStream(file).bufferedReader().use { it.readText() }
    return inputAsString
}

fun savePegasConfig(content: String, context: Context) {
    val path = context.filesDir
    val pegasDir = File(path, PEGAS_DIR)
    if (!pegasDir.exists()) {
        pegasDir.mkdirs()
    }
    val file = File(pegasDir, PEGAS_CONFIG_FILE)
    FileOutputStream(file).use {
        it.write(content.toByteArray())
    }
}

fun getPegasConfigABSPath(context: Context): String {
    val path = context.filesDir
    val pegasDir = File(path, PEGAS_DIR)
    val file = File(pegasDir, PEGAS_CONFIG_FILE)
    return file.absolutePath
}

fun getDefaultACLPath(context: Context): String {
    val path = context.filesDir
    val pegasDir = File(path, PEGAS_DIR)
    val file = File(pegasDir, PEGAS_ACL_FILE)
    if (!file.exists()) {
        val s = context.assets.open("default.acl")
        FileOutputStream(file).use {
            it.write(s.readBytes())
        }
    }
    return file.absolutePath
}

class PegasCommandSend(private var cmd: String) {

    var serverResponse: String

    @Throws(Exception::class)
    private fun writeToAndReadFromSocket(
        socket: Socket,
        writeTo: String
    ): String {
        return try {
            // write text to the socket
            val bufferedWriter =
                BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
            bufferedWriter.write(writeTo)
            bufferedWriter.flush()

            // read text from the socket
            val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val readBuffer = CharArray(4096)
            var ret = ""
            var len = 0
            do {
                len = bufferedReader.read(readBuffer)
                ret += copyValueOf(readBuffer.sliceArray(0..len - 1))
            } while (len >= 4096)

            // close the reader, and return the results as a String
            bufferedReader.close()
            ret
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Open a socket connection to the given server on the given port.
     * This method currently sets the socket timeout value to 10 seconds.
     * (A second version of this method could allow the user to specify this timeout.)
     */
    @Throws(Exception::class)
    private fun openSocket(server: String, port: Int): Socket {
        val socket: Socket

        // create a socket with a timeout
        return try {
            val inetAddress = InetAddress.getByName(server)
            val socketAddress: SocketAddress =
                InetSocketAddress(inetAddress, port)

            // create a socket
            socket = Socket()

            // this method will block no more than timeout ms.
            val timeoutInMs = 3 * 1000 // 3 seconds
            socket.connect(socketAddress, timeoutInMs)
            socket
        } catch (ste: SocketTimeoutException) {
            System.err.println("Timed out waiting for the socket.")
            ste.printStackTrace()
            throw ste
        }
    }


    init {
        try {
            val socket = openSocket(MainService.LOCAL_ADDRESS, MainService.PEGAS_CONTROL_PORT)
            serverResponse = writeToAndReadFromSocket(socket, cmd)
            socket.close()
        } catch (e: Exception) {
            serverResponse = e.toString()
            e.printStackTrace()
        }
    }
}

data class ServerInfo(
    var id: Int = 0,
    var active: Boolean = false,
    var name: String = "",
    var type: String = "",
    var connect: String = "",
    var g204: String = "",
)

suspend fun setServer(id: String): Boolean {
    var ret = false
    withContext(Dispatchers.IO) {
        val res = PegasCommandSend("set server " + id)
        if (res.serverResponse.startsWith("OK")) {
            ret = true;
        }
    }
    return ret
}

suspend fun pingServer(): Boolean {
    var ret = false
    withContext(Dispatchers.IO) {
        val res = PegasCommandSend("ping")
        if (res.serverResponse.startsWith("OK")) {
            ret = true;
        }
    }
    return ret
}

suspend fun getServers(): ArrayList<ServerInfo> {
    var servers = ArrayList<ServerInfo>()
    withContext(Dispatchers.IO)
    {
        val ret = PegasCommandSend("get servers")
        val serversStr = ret.serverResponse
        var idx = 0
        var curServer = ServerInfo()
        for (line in serversStr.split("\n")) {
            val row = line.trim()
            if (row.isEmpty()) {
                continue
            }
            if (idx % 2 == 0) {
                curServer = ServerInfo(
                    id = idx / 2,
                    active = row.startsWith("*"),
                    name = row.split(" ")[1],
                )
            } else {
                // metrics
                assert(curServer.id == idx / 2)
                val metrics = row.split("|").map { it ->
                    it.trim()
                }
                curServer.type = metrics[0]
                curServer.connect = metrics[1].split(":")[1].trim()
                curServer.g204 = metrics[2].split(":")[1].trim()
                servers.add(curServer.copy())
            }
            idx += 1
        }
    }
    return servers
}