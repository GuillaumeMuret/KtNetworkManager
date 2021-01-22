package com.test.koga.ktcpsocketlib.multicast

import com.test.koga.ktcpsocketlib.NetworkConfig
import com.test.koga.ktcpsocketlib.multicast.UdpMulticastReceiver.SetUpReceiver
import com.test.koga.ktcpsocketlib.utils.LogUtils
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.*

class UdpMulticastReceiver(private var networkConfig: NetworkConfig) {
    private var isClientConnected: Boolean = false
    private var isMulticastReceiverRunning: Boolean = false

    fun startReceiver() {
        startMulticastRunnable.run()
    }

    fun onClientConnected() {
        isClientConnected = true
    }

    private val startMulticastRunnable = Runnable {
        isClientConnected = false
        if (!isMulticastReceiverRunning) {
            val setUpReceiver = SetUpReceiver()
            setUpReceiver.start()
        }
    }

    private inner class SetUpReceiver : Thread() {
        override fun run() {
            super.run()
            isMulticastReceiverRunning = true
            LogUtils.d(this.javaClass, "Creation of socket UDP Multicast")
            var socket: MulticastSocket? = null

            // Use multicast lock in Android
            // var multicastLock: WifiManager.MulticastLock? = null
            try {
                // val wifi: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                // multicastLock = wifi.createMulticastLock(networkConfig.getUdpMulticastLockTag())
                // multicastLock.setReferenceCounted(true)
                // multicastLock.acquire()
                while (!isClientConnected) {
                    //Receive a packet
                    val recvBuf = ByteArray(15000)
                    val packet = DatagramPacket(recvBuf, recvBuf.size)
                    val group = InetAddress.getByName(networkConfig.udpMulticastAddress)
                    socket = MulticastSocket(networkConfig.udpMulticastPort)
                    socket.joinGroup(group)
                    socket.reuseAddress = true
                    socket.receive(packet)

                    // Packet received
                    val data = String(packet.data).trim { it <= ' ' }
                    LogUtils.d(javaClass, "Packet received from: " + packet.address.hostAddress + " - data = " + data)
                    if (data.isNotEmpty()
                        && data.substring(
                            0, networkConfig.udpMulticastMessageAddressPrefix.length
                        ).isNotEmpty()
                        && data.substring(
                            0, networkConfig.udpMulticastMessageAddressPrefix.length
                        ) == networkConfig.udpMulticastMessageAddressPrefix
                    ) {
                        val address: String = data.substring(
                            networkConfig.udpMulticastMessageAddressPrefix.length
                        )
                        if (address.isNotEmpty()) {
                            LogUtils.d(javaClass, "Address = $address")
                            if (address != networkConfig.tcpServerAddress) {
                                networkConfig.tcpServerAddress = address
                            }
                        }
                    }
                    socket.close()
                }
            } catch (ex: Exception) {
                LogUtils.e(LogUtils.DEBUG_TAG, "Oops -> ", ex)
                socket?.close()
            }
            // if (multicastLock != null) {
            //     multicastLock.release()
            // }
            isMulticastReceiverRunning = false
            if (!isClientConnected) {
                launchTimerRemakeConnection()
            }
        }
    }

    fun launchTimerRemakeConnection() {
        Timer().schedule(
            object : TimerTask() {
                override fun run() {
                    startMulticastRunnable.run()
                }
            }, networkConfig.twRestartMulticast.toLong()
        )
    }

    init {
        isClientConnected = false
        isMulticastReceiverRunning = false
        startReceiver()
    }
}