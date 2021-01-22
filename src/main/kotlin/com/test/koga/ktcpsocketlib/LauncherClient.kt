package com.test.koga.ktcpsocketlib

import com.test.koga.ktcpsocketlib.client.TcpClient
import com.test.koga.ktcpsocketlib.server.TcpServer
import com.test.koga.ktcpsocketlib.utils.LogUtils


object LauncherClient {
    @JvmStatic
    fun main(args: Array<String>) {
        LogUtils.d(javaClass,"Run")
        val tcpClient = TcpClient(NetworkConfig())
        readLine()
        tcpClient.destroyCommunication()
    }
}