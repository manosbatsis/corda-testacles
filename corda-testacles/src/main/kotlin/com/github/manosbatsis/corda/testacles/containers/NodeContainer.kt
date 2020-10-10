/*
 * Corda Testacles: Tools to grow some cordapp test suites.
 * Copyright (C) 2018 Manos Batsis
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
package com.github.manosbatsis.corda.testacles.containers

import com.github.manosbatsis.corda.rpc.poolboy.connection.NodeRpcConnection
import com.github.manosbatsis.corda.testacles.model.SimpleNodeConfig
import net.corda.core.identity.CordaX500Name
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

    fun createRpcConnection(user: User  = getDefaultRpcUser()): NodeRpcConnection
}