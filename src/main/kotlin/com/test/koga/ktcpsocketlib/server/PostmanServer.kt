package com.test.koga.ktcpsocketlib.server

import com.test.koga.ktcpsocketlib.NetworkConfig
import com.test.koga.ktcpsocketlib.server.socket.SocketCommunication
import com.test.koga.ktcpsocketlib.server.socket.SocketConnection
import java.io.IOException

class PostmanServer(
    /**
     * Socket state observer list
     */
    private val iTcpServerCommunicationEvent: ITcpServerCommunicationEvent,
    networkConfig: NetworkConfig
) :
    IPostmanServer {

    /**
     * Map with KEY = port and VALUE = SocketCommunication
     */
    private val socketCommunicationMap: HashMap<Int, SocketCommunication> = HashMap()

    /**
     * The socket connection
     */
    private val socketConnection: SocketConnection = SocketConnection(this, networkConfig)

    /**
     * Process called to destroy all the communication
     */
    fun destroy() {
        socketConnection.destroy()
        for (key in socketCommunicationMap.keys) {
            val socketCommunication: SocketCommunication? = socketCommunicationMap[key]
            if (socketCommunication != null && socketCommunication.isConnected) {
                socketCommunication.destroy()
            }
        }
    }

    /**
     * Process called to write message
     *
     * @param message : the message to send
     */
    fun writeMessage(port: Int, message: String) {
        val socketCommunication: SocketCommunication? = socketCommunicationMap[port]
        if (socketCommunication != null && socketCommunication.isConnected) {
            socketCommunication.writeMessage(message)
        }
    }

    /**
     * Process called to broadcast message
     *
     * @param message : the message to broadcast
     */
    fun broadcastMessage(message: String) {
        for (key in socketCommunicationMap.keys) {
            val socketCommunication: SocketCommunication? = socketCommunicationMap[key]
            if (socketCommunication != null && socketCommunication.isConnected) {
                socketCommunication.writeMessage(message)
            }
        }
    }

    /**
     * Read a message on the socket and return the message
     *
     * @return : the socket message
     * @throws IOException          : the input output exception
     * @throws NullPointerException : the null pointer exception
     */
    @Throws(IOException::class, NullPointerException::class)
    fun readMessage(port: Int): String {
        return socketCommunicationMap[port]!!.readMessage()
    }

    /**
     * Process called to get the socket state
     *
     * @return the state of the socket
     */
    fun isConnected(port: Int): Boolean {
        val socketCommunication: SocketCommunication? = socketCommunicationMap[port]
        if (socketCommunication != null) {
            return socketCommunication.isConnected
        }
        return false
    }

    /**
     * Process called to remove the socket communication. Occur on socket error
     */
    private fun removeSocketCommunication(port: Int) {
        val socketCommunication: SocketCommunication? = socketCommunicationMap[port]
        if (socketCommunication != null) {
            socketCommunication.destroy()
            socketCommunicationMap.remove(port)
        }
    }

    /**
     * Process called to notify all the observers of the socket state
     *
     * @param port : the socket port disconnected
     */
    override fun notifySocketDisconnected(port: Int) {
        // Add socket to map
        removeSocketCommunication(port)
        iTcpServerCommunicationEvent.onSocketDisconnected(port)
    }

    /**
     * Process called to notify all the observers of the socket state
     *
     * @param socketCommunication : the communication socket
     */
    override fun notifyNewSocketConnected(port: Int, address: String, socketCommunication: SocketCommunication) {
        // Add socket to map
        socketCommunicationMap[port] = socketCommunication
        iTcpServerCommunicationEvent.onSocketConnected(port, address)
    }
}