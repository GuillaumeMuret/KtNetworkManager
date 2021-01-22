package com.test.koga.ktcpsocketlib.server

import com.test.koga.ktcpsocketlib.server.socket.SocketCommunication

interface IPostmanServer {
    fun notifySocketDisconnected(port: Int)
    fun notifyNewSocketConnected(port: Int, address: String, socketCommunication: SocketCommunication)
}