package com.test.koga.ktcpsocketlib.server

interface ITcpServerCommunicationEvent {
    /**
     * Process called when a socket is connected
     */
    fun onSocketConnected(port: Int, socketAddress: String)

    /**
     * Process called when socket is disconnected
     */
    fun onSocketDisconnected(port: Int)

    /**
     * Process called when a message is received on specified socket port
     */
    fun onMessageReceived(port: Int, message: String)
}