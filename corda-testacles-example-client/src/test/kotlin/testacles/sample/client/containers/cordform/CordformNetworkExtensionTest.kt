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
package testacles.sample.client.containers.cordform

import com.github.manosbatsis.corda.testacles.containers.config.database.CordformDatabaseSettingsFactory.POSTGRES
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNetworkContainer
import com.github.manosbatsis.corda.testacles.containers.cordform.config.CordaNetworkConfig
import com.github.manosbatsis.corda.testacles.containers.cordform.config.CordformNetworkConfig
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer
import com.github.manosbatsis.corda.testacles.jupiter.*
import net.corda.core.utilities.getOrThrow
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Network
import testacles.sample.cordapp.workflow.YoDto
import testacles.sample.cordapp.workflow.YoFlow1
import testacles.sample.test.containers.cordform.TestVariations.Companion.cordaVersionOs
import java.io.File

/** Sample test using [CordformNetworkExtension] */
@ExtendWith(CordformNetworkExtension::class)
@Tags(Tag("cordform"))
// Run a single network at a time
// @ResourceLock(CordformNetworkContainer.RESOURCE_LOCK)
class CordformNetworkExtensionTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CordformNetworkExtensionTest::class.java)

        // Note: Ignored if a [CordaNetworkConfig]-annotated
        // field is present.
        @NodesImageName
        @JvmStatic
        val nodesImageName = cordaVersionOs()

        // Optional, defaults to new network
        // Note: Ignored if a [CordaNetworkConfig]-annotated
        // field is present.
        @NodesNetwork
        @JvmStatic
        val nodesNetwork: Network = Network.newNetwork()

        // Optional, defaults to auto-lookup (build/nodes, ../build/nodes)
        // Note: Ignored if a [CordaNetworkConfig]-annotated
        // field is present.
        @NodesDir
        @JvmStatic
        val nodesDir = File(System.getProperty("user.dir"))
                .parentFile.resolve("build/nodes")

        // Optional, provides the Corda network config to the extension.
        // When using this all other extension config annotations
        // willbe ignored (@NodesImageName, @NodesNetwork and @NodesDir)
        @CordaNetwork
        @JvmStatic
        val networkConfig: CordaNetworkConfig = CordformNetworkConfig(
                nodesDir = nodesDir,
                cloneNodesDir = true,
                imageName = nodesImageName,
                network = nodesNetwork,
                // Create a Postgres DB for each node (default is H2)
                databaseSettings = POSTGRES,
                clearEnv = true)

    }
    // The extension implements a ParameterResolver
    // for CordformNetworkContainer
    @Test
    fun `Can retrieve node identity`(cordformNetworkContainer: CordformNetworkContainer) {
        val nodeA: NodeContainer<*> = cordformNetworkContainer.nodes["partya"]
                ?: error("Instance not found")
        Assertions.assertTrue(nodeA.nodeIdentity.toString().contains("PartyA"))
    }

    @Test
    fun `Can send a yo`(cordformNetworkContainer: CordformNetworkContainer) {
        val nodeA = cordformNetworkContainer.getNode("partya")
        val nodeB = cordformNetworkContainer.getNode("partyb")
        val rpcOpsA = nodeA.getRpc(/* optional user or username */)
        val rpcOpsB = nodeB.getRpc(/* optional user or username */)

        // Get peers
        logger.info("Can send a yo, networkMapSnapshot for PartyA: ")
        rpcOpsA.networkMapSnapshot().forEach {
            logger.info("   Identity: ${it.legalIdentities.first()}, " +
                    "addresses: ${it.addresses.joinToString(",")}")
        }
        logger.info("Can send a yo, networkMapSnapshot for PartyB: ")
        rpcOpsB.networkMapSnapshot().forEach {
            logger.info("   Identity: ${it.legalIdentities.first()}, " +
                    "addresses: ${it.addresses.joinToString(",")}")
        }

        val yoDto = YoDto(
                recipient = nodeB.nodeIdentity,
                message = "Yo from A to B!")
        val yoState = rpcOpsA.startFlowDynamic(YoFlow1::class.java, yoDto)
                .returnValue.getOrThrow()
        Assertions.assertEquals(yoDto.message, yoState.yo)
        Assertions.assertEquals(yoDto.recipient, yoState.recipient.name)

    }
}
