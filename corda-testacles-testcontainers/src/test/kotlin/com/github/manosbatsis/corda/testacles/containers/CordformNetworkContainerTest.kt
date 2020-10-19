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
package com.github.manosbatsis.corda.testacles.containers

import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNetworkContainer
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNodeContainer
import mypackage.cordapp.workflow.YoDto
import mypackage.cordapp.workflow.YoFlow1
import net.corda.core.utilities.getOrThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File


/** An RPC-based test using [CordformNetworkContainer] */
@Testcontainers
class CordformNetworkContainerTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CordformNetworkContainerTest::class.java)

        @Container
        @JvmStatic
        val cordformNetworkContainer = CordformNetworkContainer(
                nodesDir = File(System.getProperty("user.dir"))
                        .parentFile.resolve("build/nodes"),
                // Will clone nodesDir to build/testacles/{random UUID}
                // and use that instead
                cloneNodesDir = true)
    }

    @Test
    fun `Can retrieve node identity`() {
        val nodeA: CordformNodeContainer = cordformNetworkContainer.nodes["partya"]
                ?: error("Instance not found")
        assertTrue(nodeA.nodeIdentity.toString().contains("PartyA"))
    }

    @Test
    fun `Can send a yo`() {
        val nodeA = cordformNetworkContainer.getNode("partya")
        val nodeB = cordformNetworkContainer.getNode("partyb")
        val rpcOpsA = nodeA.getRpc(/* optional user or username */)
        val yoDto = YoDto(
                recipient = nodeB.nodeIdentity,
                message = "Yo from A to B!")
        val yoState = rpcOpsA.startFlowDynamic(YoFlow1::class.java, yoDto)
                .returnValue.getOrThrow()
        assertEquals(yoDto.message, yoState.yo)
        assertEquals(yoDto.recipient, yoState.recipient.name)

    }
}
