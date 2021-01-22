package com.test.koga.ktcpsocketlib.server.socket

import com.test.koga.ktcpsocketlib.NetworkConfig
import com.test.koga.ktcpsocketlib.server.IPostmanServer
import com.test.koga.ktcpsocketlib.utils.LogUtils
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.util.logging.Handler

class SocketConnection(
    /**
     * The postman server callback
     */
    private val iPostmanServer: IPostmanServer,

    /**
     * The network configuration
     */
    private var networkConfig: NetworkConfig
    ) : AbstractSocket() {

    /**
     * The setup connection thread
     */
    private var setUpConnection: SetUpConnection? = null

    /**
     * The server socket
     */
    private var serverSocket: ServerSocket? = null

    /**
     * Process called to destroy socket connection
     */
    fun destroy() {
        closeSocket()
        if (setUpConnection != null) {
            setUpConnection!!.setRunning(false)
        }
    }

    /**
     * Process called to close socket
     */
    private fun closeSocket() {
        if (serverSocket != null) {
            try {
                serverSocket!!.close()
            } catch (e: IOException) {
                LogUtils.e(LogUtils.DEBUG_TAG, "Exception in socket connection -> close socket -> ", e)
            }
        }
    }

    /**
     * process to connect the application to the client by the socket
     */
    private inner class SetUpConnection() : Thread() {
        private var isRunning = false
        override fun run() {
            super.run()
            isRunning = true
            LogUtils.d(this.javaClass, "TCP Create socket")
            // Set up the socket
            serverSocket = null
            try {
                serverSocket = ServerSocket(
                    networkConfig.tcpServerPort,
                    networkConfig.socketQueueWait,
                    InetAddress.getByName(networkConfig.tcpServerAddress)
                )
                while (isRunning) {
                    isConnected = true
                    val mySocket = serverSocket!!.accept()
                    val socketCommunication = SocketCommunication(iPostmanServer, mySocket)
                    LogUtils.d(javaClass, "mySocket.getInetAddress() -> " + mySocket.inetAddress)
                    iPostmanServer.notifyNewSocketConnected(
                        mySocket.port,
                        mySocket.inetAddress.toString(),
                        socketCommunication
                    )
                    LogUtils.d(
                        this.javaClass,
                        "Socket connected : " + mySocket.inetAddress + " on Port => " + mySocket.port
                    )
                }
            } catch (e: IOException) {
                LogUtils.e(LogUtils.DEBUG_TAG, "Socket server tcp error", e)
                restartCommunication(networkConfig)
            }
            isRunning = false
            isConnected = false
        }

        fun setRunning(running: Boolean) {
            isRunning = running
        }

        init {
            isConnected = false
        }
    }

    fun restartCommunication(networkConfig: NetworkConfig) {
        this.networkConfig = networkConfig
        destroy()
        setUpConnection = SetUpConnection()
        setUpConnection!!.start()
    }

    /**
     * Main constructor of the server sockets
     * @param iPostmanServer : the postman callback which manage the sockets
     */
    init {
        restartCommunication(networkConfig)
    }
}