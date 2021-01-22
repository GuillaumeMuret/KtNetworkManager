package com.test.koga.ktcpsocketlib.server.socket

abstract class AbstractSocket {
    /**
     * State of the socket
     */
    var isConnected = false
        protected set

    companion object {
        /**
         * type of encodage.
         */
        protected const val ENCODAGE = "UTF-8"
    }
}