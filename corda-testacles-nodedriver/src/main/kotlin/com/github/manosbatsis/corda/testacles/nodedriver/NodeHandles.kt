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
package com.github.manosbatsis.corda.testacles.nodedriver

import net.corda.core.identity.CordaX500Name
import net.corda.testing.driver.NodeHandle

class NodeHandles(
        private val nodesByName: Map<String, NodeHandle>
): Map<String, NodeHandle> by nodesByName {

    fun findNodeByIdentity(identity: CordaX500Name): NodeHandle? =
        nodesByName.values.find {
            it.nodeInfo.legalIdentities.find { it.name == identity } != null
        }

    fun getNodeByIdentity(identity: CordaX500Name): NodeHandle =
            findNodeByIdentity(identity) ?: error("No matching node found for identity: $identity")

    fun findNodeByKey(key: String): NodeHandle? = nodesByName[key]

    fun getNodeByKey(key: String): NodeHandle =
            findNodeByKey(key) ?: error("No matching node found for key: $key")


}