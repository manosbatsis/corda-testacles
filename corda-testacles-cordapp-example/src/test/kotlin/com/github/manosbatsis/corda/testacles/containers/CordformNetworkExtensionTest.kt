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
package com.github.manosbatsis.corda.testacles.containers

import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNetworkContainer
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNodeContainer
import com.github.manosbatsis.corda.testacles.jupiter.CordformNetworkExtension
import com.github.manosbatsis.corda.testacles.jupiter.NodesDir
import com.github.manosbatsis.corda.testacles.jupiter.NodesImageName
import com.github.manosbatsis.corda.testacles.jupiter.NodesNetwork
import mypackage.cordapp.workflow.YoDto
import mypackage.cordapp.workflow.YoFlow1
import net.corda.core.utilities.getOrThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Network
import java.io.File

/** Sample test using [CordformNetworkExtension] */
@ExtendWith(CordformNetworkExtension::class)
class CordformNetworkExtensionTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CordformNetworkExtensionTest::class.java)

        // Optional, defaults to corda/corda-zulu-java1.8-4.5
        @NodesImageName
        @JvmStatic
        val nodesImageName = CordformNetworkContainer.DEFAULT_CORDA_IMAGE_NAME_4_5

        // Optional, defaults to new network
        @NodesNetwork
        @JvmStatic
        val nodesNetwork = Network.newNetwork()

        // Optional, defaults to auto-lookup (build/nodes, ../build/nodes)
        @NodesDir
        @JvmStatic
        val nodesDir = File(System.getProperty("user.dir"))
                .parentFile.resolve("build/nodes")
    }
    // The extension implements a ParameterResolver
    // for CordformNetworkContainer
    @Test
    fun `Can retrieve node identity`(cordformNetworkContainer: CordformNetworkContainer) {
        val nodeA: CordformNodeContainer = cordformNetworkContainer.nodes["partya"]
                ?: error("Instance not found")
        assertTrue(nodeA.nodeIdentity.toString().contains("PartyA"))
    }

    @Test
    fun `Can send a yo`(cordformNetworkContainer: CordformNetworkContainer) {
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
