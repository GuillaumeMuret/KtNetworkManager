package com.test.koga.ktcpsocketlib.client

internal interface ITcpClientCommunicationEvent {

    /**
     * Process called when a message is received
     */
    fun onMessageReceived(message: String?)

    /**
     * Process called when communication is established
     */
    fun onCommunicationEstablished()

    /**
     * Process called communication has failed
     */
    fun onCommunicationFailed()
}