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
