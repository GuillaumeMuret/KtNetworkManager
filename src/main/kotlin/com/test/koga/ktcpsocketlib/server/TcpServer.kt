package com.test.koga.ktcpsocketlib.server

import com.test.koga.ktcpsocketlib.NetworkConfig
import com.test.koga.ktcpsocketlib.server.socket.AbstractSocket
import com.test.koga.ktcpsocketlib.utils.LogUtils
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class TcpServer constructor(
    /**
     * The network configuration
     */
    private val networkConfig: NetworkConfig
) : ITcpServerCommunicationEvent {
    /**
     * The postman who send the messages for the client
     */
    private var postmanServer: PostmanServer? = null

    /**
     * Map of the read thread : this map contain the KEY = the socket port and the VALUE = the socket
     */
    private val mapReadThread: HashMap<Int, ReadThread>?

    /**
     * Process called to destroy the tcp server communication
     */
    fun destroy() {
        postmanServer?.destroy()
        // handlerCheckerClientConnection.removeCallbacks(runnableCheckClientCommunication)
        if (mapReadThread != null) {
            for (key in mapReadThread.keys) {
                val readThread = mapReadThread[key]
                readThread?.setStopThread(true)
            }
        }
    }

    /**
     * Process which ask to the postmanServer to read a message
     * @param port : the port of the socket where the message has to be read
     * @return : the socket message
     * @throws IOException : the input output exception
     */
    @Throws(IOException::class)
    fun readComMessage(port: Int): String? {
        return if (postmanServer?.isConnected(port) == true
        ) {
            postmanServer!!.readMessage(port)
        } else null
    }

    /**
     * Process called by the postman when a new socket is connected
     */
    override fun onSocketConnected(port: Int, socketAddress: String) {
        val readThread = ReadThread(port)
        readThread.start()
        mapReadThread!![port] = readThread
    }

    override fun onSocketDisconnected(port: Int) {
        val readThread = mapReadThread!![port]
        if (readThread != null) {
            readThread.stopTheThread = true
            mapReadThread.remove(port)
        }
    }

    override fun onMessageReceived(port: Int, message: String) {

    }

    private val runnableCheckClientCommunication: Runnable = Runnable {
        postmanServer?.broadcastMessage(networkConfig.pingMessage)
        launchTimerCheckConnection()
    }

    /**
     * Reading thread class called to read a message
     */
    private inner class ReadThread
    /**
     * Main constructor of the read thread
     * @param port : the port where the postman read
     */(
        /**
         * The socket port
         */
        private val port: Int
    ) : Thread() {
        /**
         * The boolean to stop the thread
         */
        var stopTheThread = false

        /**
         * Called to read a message and send this message to the binder man's letter box
         *
         * @throws IOException          : The Input Output exception
         * @throws NullPointerException : The null pointer exception
         */
        @Throws(IOException::class, NullPointerException::class)
        private fun manageReading() {
            // loop which read the buffer and block the task when nothing is received
            val receivedMessage: String? = readComMessage(port)
            while (!stopTheThread && receivedMessage == null);
            if (receivedMessage != null) {
                LogUtils.d(this.javaClass, "MESSAGE RECU > $receivedMessage")
                try {
                    onMessageReceived(port, receivedMessage)
                } catch (e: Exception) {
                    LogUtils.e(
                        LogUtils.DEBUG_TAG,
                        "Exception in manageReading.. Check message send or the log (maybe socket disconnected)",
                        e
                    )
                }
            }
        }

        /**
         * Process called when a "start" of the thread occur
         */
        override fun run() {
            LogUtils.d(this.javaClass, "TCP Launch Read Thread on port $port")
            try {
                // infinite loop. If a network problem occur : the infinite loop die
                while (!stopTheThread) {
                    // manage the message read
                    manageReading()
                }
            } catch (npe: NullPointerException) {
                LogUtils.d(this.javaClass, "TCP socket closed npe...")
            } catch (ioe: IOException) {
                LogUtils.d(this.javaClass, "TCP Socket closed ioe...")
            }
        }

        fun setStopThread(stopThread: Boolean) {
            this.stopTheThread = stopThread
        }
    }

    private fun launchTimerCheckConnection() {
        LogUtils.d(this.javaClass,"TimerCheckConnection")
        Timer().schedule(
            object : TimerTask() {
                override fun run() {
                    runnableCheckClientCommunication.run()
                }
            }, networkConfig.twPingCheckCommunication.toLong()
        )
    }

    /**
     * Builder of the communication
     */
    init {
        mapReadThread = HashMap()
        postmanServer = PostmanServer(this, networkConfig)
        launchTimerCheckConnection()
    }
}