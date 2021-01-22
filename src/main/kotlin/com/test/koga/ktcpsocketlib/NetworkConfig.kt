package com.test.koga.ktcpsocketlib

class NetworkConfig {
    var twSetupConnection: Int = DEFAULT_TW_SETUP_CONNECTION
    var twRemakeConnection: Int = DEFAULT_TW_REMAKE_CONNECTION
    var twPingCheckCommunication: Int = DEFAULT_TW_PING_CHECK_COMMUNICATION
    var tcpServerAddress: String = DEFAULT_TCP_SERVER_ADDRESS
    var tcpServerPort: Int = DEFAULT_TCP_SERVER_PORT
    var pingMessage: String = DEFAULT_TCP_PING_MESSAGE
    var udpMulticastPort: Int = DEFAULT_UDP_MULTICAST_PORT
    var udpMulticastAddress: String = DEFAULT_UDP_MULTICAST_ADDRESS
    var udpMulticastMessageAddressPrefix: String = DEFAULT_UDP_MULTICAST_MESSAGE_ADDRESS_PREFIX
    var udpMulticastLockTag: String = DEFAULT_MULTICAST_LOCK_TAG
    var twRestartMulticast: Int = DEFAULT_TW_RESTART_MULTICAST
    var socketQueueWait: Int = DEFAULT_SOCKET_QUEUE_WAIT

    companion object {
        private const val DEFAULT_TW_REMAKE_CONNECTION = 1000 // 1s
        private const val DEFAULT_TW_SETUP_CONNECTION = 5000 // 5s
        private const val DEFAULT_TW_PING_CHECK_COMMUNICATION = 1000 // 1s
        private const val DEFAULT_TCP_SERVER_ADDRESS = "0.0.0.0"
        private const val DEFAULT_TCP_SERVER_PORT = 13579
        private const val DEFAULT_TCP_PING_MESSAGE = "{\"process\":\"checkTcpConnection\"}"
        private const val DEFAULT_UDP_MULTICAST_PORT = 13580
        private const val DEFAULT_UDP_MULTICAST_ADDRESS = "228.5.6.8"
        private const val DEFAULT_UDP_MULTICAST_MESSAGE_ADDRESS_PREFIX = "server_address="
        private const val DEFAULT_MULTICAST_LOCK_TAG = "multicastLockTcpServer"
        private const val DEFAULT_TW_RESTART_MULTICAST = 1000 // 1s
        private const val DEFAULT_SOCKET_QUEUE_WAIT = 100
    }
}