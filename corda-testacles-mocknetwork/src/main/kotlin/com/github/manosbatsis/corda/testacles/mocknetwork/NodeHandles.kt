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
package com.github.manosbatsis.corda.testacles.mocknetwork

import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.testing.node.StartedMockNode

/**
 * The[mockNetwork]'s [StartedMockNode]s, each mapped by multiple keys:
 *
 * - identity ([Party])
 * - identity name ([CordaX500Name]
 * - identity name (string representation)
 * - organization name
 * - organization name in lower case without spaces
 *
 * In other words `nodes["O=PartyA, L=Athens, C=GR"]` is the same as `nodes["partya"]`
 */
open class NodeHandles(
        private val nodes: Map<Any, StartedMockNode> = emptyMap()
){

    fun findNodeByIdentity(identity: CordaX500Name): StartedMockNode? =
            nodes[identity]

    fun getNodeByIdentity(identity: CordaX500Name): StartedMockNode =
            findNodeByIdentity(identity) ?: error("No matching node found for identity: $identity")

    fun findNodeByKey(key: String): StartedMockNode? = nodes[key]

    fun getNodeByKey(key: String): StartedMockNode =
            findNodeByKey(key) ?: error("No matching node found for key: $key")


}