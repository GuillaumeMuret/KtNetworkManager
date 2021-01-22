package com.test.koga.ktcpsocketlib.client

import com.test.koga.ktcpsocketlib.NetworkConfig
import com.test.koga.ktcpsocketlib.multicast.UdpMulticastReceiver
import com.test.koga.ktcpsocketlib.utils.LogUtils
import java.io.IOException
import java.util.*

class TcpClient(
    /**
     * The network config for the communication
     */
    private val networkConfig: NetworkConfig
) : ITcpClientCommunicationEvent {
    /**
     * The postman who send the messages for the UI
     */
    private var postmanClient: PostmanClient? = null

    /**
     * The UdpBroadcastReceiver
     */
    private val udpMulticastReceiver: UdpMulticastReceiver

    /**
     * The communication event of the app service
     */
    private val iTcpClientCommunicationEvent: ITcpClientCommunicationEvent

    /**
     * Flag to know if client is connected or not
     */
    var isConnected = false
        private set

    private fun restartCommunication() {
        endCommunication()
        postmanClient?.restartCommunication()
    }

    /**
     * Process which ask to the postman to read a message
     *
     * @return the received message
     */
    @Throws(IOException::class)
    private fun readComMessage(): String? {
        return if (postmanClient?.isConnected == true) {
            postmanClient?.readMessage()
        } else null
    }

    @Synchronized
    fun sendMessage(message: String) {
        postmanClient?.writeMessage(message)
    }

    fun destroyCommunication() {
        postmanClient?.closeSocket()
        // handlerCheckerConnection.removeCallbacks(runnableCheckCommunication)
    }

    /**
     * Process called to close the socket
     */
    private fun endCommunication() {
        postmanClient?.closeSocket()
    }

    private val runnableCheckCommunication: Runnable = Runnable {
        if (postmanClient?.isConnected == true) {
            sendMessage(networkConfig.pingMessage)
        }
        launchTimerCheckConnection()
    }

    private fun launchTimerCheckConnection() {
        LogUtils.d(this.javaClass,"launchTimeCheckConnection")
        Timer().schedule(
            object : TimerTask() {
                override fun run() {
                    runnableCheckCommunication.run()
                }
            }, networkConfig.twPingCheckCommunication.toLong()
        )
    }

    /**
     * Nested class reading thread called to read a message on socket
     */
    private inner class ReadThread : Thread() {
        /**
         * The received message from the socket
         */
        var receivedMessage: String? = null

        /**
         * Called to read a message and send this message to the binder man's letter box
         *
         * @throws IOException
         * @throws NullPointerException
         */
        @Throws(IOException::class, NullPointerException::class)
        private fun manageReading() {

            // loop which read the buffer and block the task when nothing is received
            while (readComMessage().also { receivedMessage = it } == null);
            // LogHelper.d(this.getClass(),"MESSAGE RECU >" + receivedMessage);
            iTcpClientCommunicationEvent.onMessageReceived(receivedMessage)
        }

        /**
         * Process called when a "start" of the thread occur
         */
        override fun run() {
            try {
                // infinite loop. If a network problem occur : the infinite loop die
                while (true) {
                    // manage the message read
                    manageReading()
                }
            } catch (e: Exception) {
                LogUtils.e(LogUtils.DEBUG_TAG, "Socket error", e)
                onCommunicationFailed()
            }
        }
    }

    override fun onMessageReceived(message: String?) {
        LogUtils.d(javaClass, "MESSAGE RECU > $message")
    }

    override fun onCommunicationEstablished() {
        isConnected = true
        udpMulticastReceiver.onClientConnected()
        // The thread which read a message on the postman's socket
        val readThread: ReadThread = ReadThread()
        readThread.start()
        // iTcpClientCommunicationEvent.onCommunicationEstablished()
    }

    override fun onCommunicationFailed() {
        isConnected = false
        udpMulticastReceiver.startReceiver()
        restartCommunication()
    }

    /**
     * Builder of the communication
     */
    init {
        LogUtils.d(this.javaClass,"init")
        // The communication event for the service
        iTcpClientCommunicationEvent = this

        // Udp broadcast receiver for board address
        udpMulticastReceiver = UdpMulticastReceiver(networkConfig)

        //The postman
        postmanClient = PostmanClient(this, networkConfig)

        // Check communication with a ping
        launchTimerCheckConnection()
    }
}