package com.test.koga.ktcpsocketlib.client

import com.test.koga.ktcpsocketlib.NetworkConfig
import com.test.koga.ktcpsocketlib.utils.LogUtils
import java.io.*
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

internal class PostmanClient(
    /**
     * The tcp callback
     */
    private val iTcpClientCommunicationEvent: ITcpClientCommunicationEvent,
    /**
     * Network configuration
     */
    var networkConfig: NetworkConfig
) {
    /**
     * State of the socket
     */
    var isConnected : Boolean = false

    /**
     * The socket used for the connection
     */
    private var mySocket: Socket? = null

    /**
     * the buffer used to read a message on the socket
     */
    private var bufferedReader: BufferedReader? = null

    /**
     * the buffer used to write a message on the socket
     */
    private var bufferedWriter: BufferedWriter? = null

    /**
     * the SetUpConnexion thread
     */
    private var setUpConnection: SetUpConnection

    /**
     * Process called to restart communication on socket
     */
    fun restartCommunication() {
        isConnected = false
        setUpConnection = SetUpConnection()
        setUpConnection.start()
    }

    /**
     * write the message on the socket
     * @param message
     */
    fun writeMessage(message: String) {
        Write(message).start()
    }

    /**
     * read a message on the socket and return the message
     * @return the message
     * @throws IOException
     */
    @Throws(IOException::class, NullPointerException::class)
    fun readMessage(): String {
        return bufferedReader!!.readLine()
    }

    /**
     * Nested class to set up the connection with socket
     */
    private inner class SetUpConnection : Thread() {
        override fun run() {
            super.run()
            LogUtils.d(this.javaClass, "Creation of socket TCP ipServer = " + networkConfig.tcpServerAddress)
            isConnected = false

            // Set up the socket
            try {
                mySocket = Socket()
                mySocket!!.connect(
                    InetSocketAddress(networkConfig.tcpServerAddress, networkConfig.tcpServerPort),
                    networkConfig.twSetupConnection
                )
                LogUtils.d(this.javaClass, "Socket Created")
                bufferedReader = BufferedReader(InputStreamReader(mySocket!!.getInputStream(), ENCODE))
                bufferedWriter = BufferedWriter(OutputStreamWriter(mySocket!!.getOutputStream(), ENCODE))
                isConnected = true
                iTcpClientCommunicationEvent.onCommunicationEstablished()
            } catch (ce: ConnectException) {
                LogUtils.e(LogUtils.DEBUG_TAG, "Socket ConnectException -> " + ce.message)
                isConnected = false
                launchTimerRemakeConnection()
            } catch (e: Exception) {
                LogUtils.e(LogUtils.DEBUG_TAG, "Socket Exception ${e.message}")
                isConnected = false
                iTcpClientCommunicationEvent.onCommunicationFailed()
            }
        }
    }

    fun launchTimerRemakeConnection() {
        Timer().schedule(
            object : TimerTask() {
                override fun run() {
                    iTcpClientCommunicationEvent.onCommunicationFailed()
                }
            }, networkConfig.twRemakeConnection.toLong()
        )
    }

    /**
     * Nested class write on the BufferWriter to send a message
     */
    private inner class Write
    /**
     * Constructor of the Thread to write a message
     * @param messageToSend : the message to send
     */(
        /**
         * The message to send
         */
        private val messageToSend: String?
    ) : Thread() {
        /**
         * Called when the client is executed
         */
        @Synchronized
        override fun run() {
            if (mySocket != null) {
                if (!mySocket!!.isClosed && bufferedWriter != null && isConnected) {
                    if(messageToSend != null) {
                        try {
                            bufferedWriter!!.write(messageToSend, 0, messageToSend.length)
                            bufferedWriter!!.newLine()
                            bufferedWriter!!.flush()
                        } catch (e: IOException) {
                            isConnected = false
                            LogUtils.e(LogUtils.DEBUG_TAG, "WRITE ERROR -> Socket may be closed")
                            iTcpClientCommunicationEvent.onCommunicationFailed()
                        }
                    }
                } else {
                    isConnected = false
                }
            }
        }
    }

    /**
     * Process called to close the socket
     */
    fun closeSocket() {
        if (mySocket != null) {
            try {
                mySocket!!.close()
                isConnected = false
            } catch (e: IOException) {
                LogUtils.e(LogUtils.DEBUG_TAG, "IOException closeSocket socket !", e)
            }
        }
        if (bufferedReader != null) {
            try {
                bufferedReader!!.close()
            } catch (e: IOException) {
                LogUtils.e(LogUtils.DEBUG_TAG, "IOException closeSocket reader !", e)
            }
        }
        if (bufferedWriter != null) {
            try {
                bufferedWriter!!.close()
            } catch (e: IOException) {
                LogUtils.e(LogUtils.DEBUG_TAG, "IOException closeSocket writer !", e)
            }
        }
    }

    companion object {
        /**
         * type of encoding.
         */
        private const val ENCODE = "UTF-8"
    }

    /**
     * Builder of the Postman
     */
    init {
        isConnected = false
        setUpConnection = SetUpConnection()
        setUpConnection.start()
    }
}