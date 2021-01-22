package com.test.koga.ktcpsocketlib

import com.test.koga.ktcpsocketlib.server.TcpServer
import com.test.koga.ktcpsocketlib.utils.LogUtils


object LauncherServer {
    @JvmStatic
    fun main(args: Array<String>) {
        LogUtils.d(javaClass,"Run")
        val tcpServer = TcpServer(NetworkConfig())
        readLine()
        tcpServer.destroy()
    }
}