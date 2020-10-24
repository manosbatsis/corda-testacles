package com.github.manosbatsis.corda.testacles.containers.config.network

import com.github.manosbatsis.corbeans.test.containers.ConfigUtil
import com.github.manosbatsis.corda.testacles.containers.config.data.ConfigObjectData
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValue
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.node.services.config.NodeRpcSettings
import net.corda.nodeapi.BrokerRpcSslOptions

class RpcSettings(
        val ssl: BrokerRpcSslOptions? = null
): ConfigObjectData {

    override fun asConfigValue(target: Config): ConfigValue {
        val rpcSettings = NodeRpcSettings(
                address = NetworkHostAndPort(Companion.RPC_HOST, Companion.RPC_PORT),
                adminAddress = NetworkHostAndPort(Companion.RPC_HOST, Companion.RPC_ADMIN_PORT),
                ssl = ssl, // TODO
                useSsl = ssl != null)

        val config: Config = ConfigFactory.empty()
                .withValue("address", ConfigUtil.valueFor(rpcSettings.address.toString()))
                .withValue("adminAddress", ConfigUtil.valueFor(rpcSettings.adminAddress.toString()))

        return config.root()
    }
}

class P2pAddress(private val nodeHostName: String): ConfigObjectData {

    override fun asConfigValue(target: Config): ConfigValue {
        return ConfigUtil.valueFor("$nodeHostName:${NodeContainer.P2P_PORT}")
    }
}