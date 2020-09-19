/*
 * Corda Testacles: Test containers and tools to help cordapps grow.
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
package com.github.manosbatsis.corbeans.test.containers

import com.autodsl.annotation.AutoDsl
import com.autodsl.annotation.AutoDslCollection
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.node.services.config.NodeRpcSettings
import net.corda.nodeapi.internal.config.User
import java.util.LinkedList
import java.util.Properties


@AutoDsl(dslName="simpleNodeConf")
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