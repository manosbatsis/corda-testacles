package com.github.manosbatsis.corda.testacles.containers.node

import com.github.manosbatsis.corda.rpc.poolboy.config.NodeParams
import com.github.manosbatsis.corda.rpc.poolboy.connection.LazyNodeRpcConnection
import com.github.manosbatsis.corda.rpc.poolboy.pool.connection.NodeRpcConnectionConfig
import com.github.manosbatsis.corda.testacles.model.SimpleNodeConfig
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.nodeapi.internal.config.User

interface NodeContainer {
    companion object{
        const val P2P_PORT = 10002
        const val RPC_PORT = 10003
        const val RPC_ADMIN_PORT = 10043
        const val RPC_HOST = "0.0.0.0"
        const val NODE_CONF_FILENAME_CUSTOM = "testacles-node.conf"
        const val NODE_CONF_FILENAME_DEFAULT = "node.conf"

        fun createRpcConnection(
                nodeContainer: NodeContainer,
                user: User = nodeContainer.getDefaultRpcUser()
        ) = with(nodeContainer){
            LazyNodeRpcConnection(
                    NodeRpcConnectionConfig(
                            nodeParams = NodeParams.mergeParams(NodeParams(
                                    partyName = nodeIdentity.toString(),
                                    username = user.username,
                                    password = user.password,
                                    address = rpcAddress,
                                    adminAddress = rpcAddress,
                                    eager = false)),
                            targetLegalIdentity = nodeIdentity))
        }
    }

    val nodeName: String
    val nodeIdentity: CordaX500Name
    val simpleNodeConfig: SimpleNodeConfig
    val rpcAddress: String
    val rpcNetworkHostAndPort: NetworkHostAndPort
    val rpcUsers: List<User>

    /** Get default user user credentials, try for ALL permissions first */
    fun getDefaultRpcUser(): User =
            rpcUsers.find { it.permissions.contains("ALL") }
                    ?: simpleNodeConfig.rpcUsers.first()

    fun getRpc(username: String): CordaRPCOps {
        val user = rpcUsers.find { it.username == username }
                ?: error("Username does not exist: $username")
        return getRpc(user)
    }

    fun getRpc(user: User = getDefaultRpcUser()): CordaRPCOps
}