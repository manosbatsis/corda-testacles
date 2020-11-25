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

import com.github.manosbatsis.corda.testacles.containers.ConfigUtil
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