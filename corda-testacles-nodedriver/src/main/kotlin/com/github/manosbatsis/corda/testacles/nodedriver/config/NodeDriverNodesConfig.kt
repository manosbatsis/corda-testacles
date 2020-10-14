/*
 * Corda Testacles: Test suite toolkit for Corda developers.
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
package com.github.manosbatsis.corda.testacles.nodedriver.config

import com.autodsl.annotation.AutoDsl
import com.autodsl.annotation.AutoDslCollection
import com.github.manosbatsis.corda.rpc.poolboy.config.NodeParams
import com.github.manosbatsis.corda.rpc.poolboy.config.PoolParams
import java.util.LinkedList


interface NodeDriverNodesConfigProvider {
    fun nodesConfig(): NodeDriverNodesConfig
}

@AutoDsl("nodeDriver")
class NodeDriverNodesConfig(
        @AutoDslCollection(concreteType = LinkedList::class)
        var cordapPackages: List<String> = mutableListOf(),
        var nodes: Map<String, NodeParams> = mutableMapOf(),
        var bnmsServiceType: String? = null,
        var notarySpec: TestNotaryProperties = TestNotaryProperties(),
        var flowOverrides: List<String> = mutableListOf(),
        var poolParams: PoolParams = PoolParams(),
        val logLevelOverride: String = "warn"
) : NodeDriverNodesConfigProvider {

    override fun nodesConfig(): NodeDriverNodesConfig = this

    override fun toString(): String {
        return "NodeDriverNodesConfig(cordapPackages=$cordapPackages, " +
                "nodes=$nodes, " +
                "notarySpec=$notarySpec, " +
                "flowOverrides=${flowOverrides}), " +
                "bnmsServiceType=${bnmsServiceType}, " +
                "poolParams=${poolParams}"
    }
}