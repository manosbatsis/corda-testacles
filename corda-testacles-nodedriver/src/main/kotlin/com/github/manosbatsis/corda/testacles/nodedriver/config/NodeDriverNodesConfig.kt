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
package com.github.manosbatsis.corda.testacles.nodedriver.config

import com.autodsl.annotation.AutoDsl
import com.autodsl.annotation.AutoDslCollection
import com.github.manosbatsis.corda.rpc.poolboy.config.NodeParams
import com.github.manosbatsis.corda.rpc.poolboy.config.PoolParams
import java.util.LinkedList


interface NodeDriverNodesConfig {
    var cordapPackages: List<String>
    var nodes: Map<String, NodeParams>
    var bnmsServiceType: String?
    var notarySpec: TestNotaryProperties
    var flowOverrides: List<String>
    var poolParams: PoolParams
    val minimumPlatformVersion: Int
}

@AutoDsl("nodeDriverConfig")
open class SimpleNodeDriverNodesConfig(
        @AutoDslCollection(concreteType = LinkedList::class)
        override var cordapPackages: List<String> = mutableListOf(),
        override var nodes: Map<String, NodeParams> = mutableMapOf(),
        override var bnmsServiceType: String? = null,
        override var notarySpec: TestNotaryProperties = TestNotaryProperties(),
        override var flowOverrides: List<String> = mutableListOf(),
        override var poolParams: PoolParams = PoolParams(),
        override val minimumPlatformVersion: Int = 5
) : NodeDriverNodesConfig {

    override fun toString(): String {
        return "${this.javaClass.simpleName}(cordapPackages=$cordapPackages, " +
                "nodes=$nodes, " +
                "notarySpec=$notarySpec, " +
                "flowOverrides=${flowOverrides}), " +
                "bnmsServiceType=${bnmsServiceType}, " +
                "poolParams=${poolParams} " +
                "minimumPlatformVersion=${minimumPlatformVersion}"
    }
}