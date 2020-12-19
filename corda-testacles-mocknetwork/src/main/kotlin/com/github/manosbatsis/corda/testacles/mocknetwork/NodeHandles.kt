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

import com.github.manosbatsis.corda.testacles.common.jupiter.AbstractNodesMap
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode


/**
 * The[mockNetwork]'s [StartedMockNode]s, each mapped by multiple keys:
 *
 * - the original input key
 * - identity ([Party])
 * - identity name ([CordaX500Name]
 * - identity name (string representation)
 * - organization name
 * - organization name with lower case first char, without spaces
 * - organization name in lower case without spaces
 *
 * In other words `getNode["O=PartyA, L=Athens, C=GR"]` is the same as `getNode["partya"]`
 */
class NodeHandles(
    val network: MockNetwork,
    input: Map<String, StartedMockNode>
): AbstractNodesMap<StartedMockNode>(input){
    override fun getIdentity(of: StartedMockNode): Party =
            of.info.legalIdentities.first()
}