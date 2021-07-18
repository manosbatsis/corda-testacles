/*
 * Corda Testacles: Simple conveniences for your Corda Test Suites;
 * because who doesn't need to grow some more of those.
 *
 * Copyright (C) 2020 Manos Batsis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package com.github.manosbatsis.corda.testacles.containers.node

import com.github.dockerjava.api.model.Bind
import com.github.manosbatsis.corda.rpc.poolboy.config.NodeParams
import com.github.manosbatsis.corda.rpc.poolboy.connection.LazyNodeRpcConnection
import com.github.manosbatsis.corda.rpc.poolboy.pool.connection.NodeRpcConnectionConfig
import com.github.manosbatsis.corda.testacles.common.corda.SimpleNodeConfig
import com.github.manosbatsis.corda.testacles.containers.util.Version
import com.typesafe.config.Config
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.nodeapi.internal.config.User
import org.testcontainers.containers.Container
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget
import org.testcontainers.lifecycle.Startable
import java.io.File

interface NodeContainer<SELF : NodeContainer<SELF>>: Container<SELF>, AutoCloseable, WaitStrategyTarget, Startable {
    companion object {
        const val P2P_PORT = 10200
        const val RPC_PORT = 10201
        const val RPC_ADMIN_PORT = 10202
        const val RPC_HOST = "0.0.0.0"
        const val NODE_CONF_FILENAME_DEFAULT = "node.conf"

        fun createRpcConnection(
                nodeContainer: NodeContainer<*>,
                user: User = nodeContainer.getDefaultRpcUser()
        ) = createRpcConnection(
                    nodeIdentity = nodeContainer.nodeIdentity,
                    rpcAddress = nodeContainer.rpcAddress,
                    eager = false,
                    disableGracefulReconnect = true,
                    user = user)


        fun createRpcConnection(
                nodeIdentity: CordaX500Name,
                rpcAddress: String,
                eager: Boolean,
                disableGracefulReconnect: Boolean,
                user: User
        ) = LazyNodeRpcConnection(
                NodeRpcConnectionConfig(
                        nodeParams = NodeParams.mergeParams(NodeParams(
                                partyName = nodeIdentity.toString(),
                                username = user.username,
                                password = user.password,
                                address = rpcAddress,
                                adminAddress = rpcAddress,
                                eager = eager,
                                disableGracefulReconnect = disableGracefulReconnect)),
                        targetLegalIdentity = nodeIdentity))

    }

    val isEnterprise: Boolean
    val version: Version
    val nodeName: String
    val config: Config
    val nodeIdentity: CordaX500Name
    val simpleNodeConfig: SimpleNodeConfig
    val rpcAddress: String
    val rpcNetworkHostAndPort: NetworkHostAndPort
    val rpcUsers: List<User>

    /** Set the [Bind] entries for the container. Overrides should call `super`. */
    fun getBinds(nodeDir: File): List<Bind>

    /** Add environment variables to the container. Overrides should call `super`. */
    fun addEnvVars()

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