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

import com.github.manosbatsis.corda.rpc.poolboy.config.NodeParams
import com.github.manosbatsis.corda.rpc.poolboy.config.PoolParams
import com.github.manosbatsis.corda.testacles.nodedriver.config.TestNotaryProperties
import com.github.manosbatsis.corda.testacles.nodedriver.config.nodeDriver
import com.github.manosbatsis.corda.testacles.nodedriver.jupiter.NodeDriverExtensionConfig
import com.github.manosbatsis.corda.testacles.nodedriver.jupiter.NodeDriverNetworkExtension
import com.github.manosbatsis.partiture.flow.PartitureFlow
import mypackage.cordapp.contract.YoContract
import mypackage.cordapp.workflow.YoDto
import mypackage.cordapp.workflow.YoFlow1
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.driver.NodeHandle
import net.corda.testing.driver.internal.incrementalPortAllocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory

/** Sample test using [NodeDriverNetworkExtension] */
@ExtendWith(NodeDriverNetworkExtension::class)
class NodeDriverNetworkExtensionTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(NodeDriverNetworkExtensionTest::class.java)
        val portAllocation = incrementalPortAllocation()
        private fun CordaX500Name.toNode() = NodeParams.mergeParams(NodeParams(
                partyName = "${this}",
                username = "user1",
                password = "test",
                address = "${portAllocation.nextHostAndPort()}",
                adminAddress = "${portAllocation.nextHostAndPort()}",
                disableGracefulReconnect = true) )

        @NodeDriverExtensionConfig
        @JvmStatic
        val nodeDriverConfig = nodeDriver {
            cordapPackages = listOf(YoContract::class.java.`package`.name,
                    PartitureFlow::class.java.`package`.name)
            nodes = mapOf("partya" to ALICE_NAME.toNode(), "partyb" to BOB_NAME.toNode())
            notarySpec = TestNotaryProperties()
            flowOverrides = emptyList()
            poolParams = PoolParams()
            logLevelOverride = "warn"
        }
    }

    @Test
    fun `Can retrieve node identity`(nodeHandles: NodeHandles) {
        val nodeA: NodeHandle = nodeHandles.getNodeByKey("partya")
        assertTrue(nodeA.nodeInfo.legalIdentities.isNotEmpty())
    }

    @Test
    fun `Can send a yo`(nodeHandles: NodeHandles) {
        val nodeA = nodeHandles.getNodeByKey("partya")
        val nodeB = nodeHandles.getNodeByKey("partyb")
        val yoDto = YoDto(
                recipient = nodeB.nodeInfo.legalIdentities.first().name,
                message = "Yo from A to B!")
        val yoState = nodeA.rpc.startFlowDynamic(YoFlow1::class.java, yoDto)
                .returnValue.getOrThrow()
        assertEquals(yoDto.message, yoState.yo)
        assertEquals(yoDto.recipient, yoState.recipient.name)

    }
}
