package com.github.manosbatsis.corda.testacles.containers.config.network

import com.github.manosbatsis.corda.testacles.containers.config.data.ConfigObjectData
import com.github.manosbatsis.corda.testacles.containers.config.data.ConfigObjectDataContributor
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion.P2P_PORT
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion.RPC_ADMIN_PORT
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion.RPC_HOST
import net.corda.nodeapi.BrokerRpcSslOptions

/**
 * Modify the existing node.conf as follows:
 *
 * - RPC address: [RPC_HOST]:[RPC_PORT]
 * - RPC admin address: [RPC_HOST]:[RPC_ADMIN_PORT]
 * - P2P address: [nodeHostName]:[P2P_PORT]
 */
open class NetworkConfigContributor(
        nodeHostName: String,
        ssl: BrokerRpcSslOptions? = null
): ConfigObjectDataContributor {
    override val dataEntries: List<ConfigObjectData> = listOf(
            P2pAddress(nodeHostName), RpcSettings(ssl))
}
