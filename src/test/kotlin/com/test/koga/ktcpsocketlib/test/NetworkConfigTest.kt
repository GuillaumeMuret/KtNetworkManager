package com.test.koga.ktcpsocketlib.test

import com.test.koga.ktcpsocketlib.NetworkConfig

import junit.framework.TestCase
import org.junit.Test

class NetworkConfigTest : TestCase() {

    /**
     * Test network config
     */
    @Test
    fun testNetworkConfig() {
        val networkConfig = NetworkConfig()
        assertEquals(networkConfig.tcpServerPort,13579)
    }
}