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
package com.github.manosbatsis.corda.testacles.common.corda

import com.autodsl.annotation.AutoDsl
import com.autodsl.annotation.AutoDslCollection
import com.typesafe.config.Config
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.node.services.config.NodeConfiguration
import net.corda.node.services.config.NodeRpcSettings
import net.corda.node.services.config.SecurityConfiguration
import net.corda.node.services.config.parseAsNodeConfiguration
import net.corda.nodeapi.internal.config.User
import java.util.Properties

/**
 * Simple holder class to capture the node configuration both as the raw [Config] object and the parsed [NodeConfiguration].
 * Keeping [Config] around is needed as the user may specify extra config options not specified in [NodeConfiguration].
 */
class NodeConfig(val typesafe: Config) {
        val corda: NodeConfiguration = typesafe.parseAsNodeConfiguration().value()
}

@AutoDsl(dslName = "simpleNodeConf")
data class SimpleNodeConfig(
        var myLegalName: CordaX500Name,
        var p2pAddress: NetworkHostAndPort,
        var rpcSettings: NodeRpcSettings,
        /** This is not used by the node but by the webserver which looks at node.conf. */
        var webAddress: NetworkHostAndPort? = null,
        var notary: NotaryService? = null,
        var h2port: Int? = null,
        @AutoDslCollection(concreteType = ArrayList::class)
        var rpcUsers: MutableList<User> = mutableListOf(),
        val security: SecurityConfiguration?,
        /** Pass-through for generating node.conf with external DB */
        var dataSourceProperties: Properties? = null,
        var database: Properties? = null,
        var systemProperties: Map<String, Any?>? = null,
        var devMode: Boolean = true,
        var detectPublicIp: Boolean? = false,
        var useTestClock: Boolean? = true
)

@AutoDsl
data class NotaryService(var validating: Boolean)