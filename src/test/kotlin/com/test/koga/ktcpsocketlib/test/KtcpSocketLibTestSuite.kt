package com.test.koga.ktcpsocketlib.test

import com.test.koga.ktcpsocketlib.test.client.ClientTestSuite
import com.test.koga.ktcpsocketlib.test.server.ServerTestSuite
import org.junit.runners.Suite

import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses


@RunWith(Suite::class)
@SuiteClasses(
    ClientTestSuite::class,
    ServerTestSuite::class,
    NetworkConfigTest::class
)
class KtcpSocketLibTestSuite