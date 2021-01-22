package com.test.koga.ktcpsocketlib.server.socket

import com.test.koga.ktcpsocketlib.server.IPostmanServer
import com.test.koga.ktcpsocketlib.utils.LogUtils
import java.io.*
import java.net.Socket
import java.net.SocketTimeoutException

class SocketCommunication(
    /**
     * The postman server callback
     */
    private val iPostmanServer: IPostmanServer,

    /**
     * The socket used for the communication
     */
    private val mySocket: Socket
) : AbstractSocket() {

    /**
     * the buffer used to read a message on the socket
     */
    private var bufferedReader: BufferedReader? = null

    /**
     * the buffer used to write a message on the socket
     */
    private var bufferedWriter: BufferedWriter? = null

    /**
     * Process called to destroy current communication
     */
    fun destroy() {
        try {
            mySocket.close()
        } catch (e: IOException) {
            LogUtils.e(LogUtils.DEBUG_TAG, "IO Exception", e)
        }
    }

    /**
     * Process called to write a message on the socket
     * @param message : the message to send
     */
    fun writeMessage(message: String) {
        val writingThread = Write(message)
        writingThread.start()
    }

    /**
     * read a message on the socket and return the message
     *
     * @return the message
     * @throws IOException : exception of Input Output
     * @throws NullPointerException : the null pointer exception
     */
    @Throws(IOException::class, NullPointerException::class)
    fun readMessage(): String {
        return bufferedReader!!.readLine()
    }

    /**
     * Write on the BufferWriter to send a message
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
            if (!mySocket.isClosed) {
                if(messageToSend != null) {
                    try {
                        bufferedWriter!!.write(messageToSend, 0, messageToSend.length)
                        bufferedWriter!!.newLine()
                        bufferedWriter!!.flush()
                    } catch (e: IOException) {
                        isConnected = false
                        LogUtils.e(LogUtils.DEBUG_TAG, "WRITE ERROR -> Socket may be closed")
                        iPostmanServer.notifySocketDisconnected(mySocket.port)
                    }
                }
            } else {
                isConnected = false
            }
        }
    }

    /**
     * Main constructor of the connexion socket
     *
     * @param mySocket : socket of the connexion
     */
    init {
        try {
            bufferedReader = BufferedReader(
                InputStreamReader(this.mySocket.getInputStream(), MESSAGE_ENCODAGE)
            )
            bufferedWriter = BufferedWriter(
                OutputStreamWriter(this.mySocket.getOutputStream(), MESSAGE_ENCODAGE)
            )
            isConnected = true
        } catch (ste: SocketTimeoutException) {
            // appears when the socket reach timeout limit
            LogUtils.e(LogUtils.DEBUG_TAG, "SocketTimeoutException : not found", ste)
            isConnected = false
        } catch (e: IOException) {
            // appears when the socket is already connected
            LogUtils.e(LogUtils.DEBUG_TAG, "SocketError debug", e)
            isConnected = false
        }
    }

    companion object {
        private const val MESSAGE_ENCODAGE = "UTF-8"
    }
}