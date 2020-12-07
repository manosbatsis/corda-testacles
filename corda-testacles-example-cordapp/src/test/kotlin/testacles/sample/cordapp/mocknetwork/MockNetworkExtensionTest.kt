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
package testacles.sample.cordapp.mocknetwork

import com.github.manosbatsis.corda.testacles.mocknetwork.NodeHandles
import com.github.manosbatsis.corda.testacles.mocknetwork.config.CordaX500Names
import com.github.manosbatsis.corda.testacles.mocknetwork.config.MockNetworkConfig
import com.github.manosbatsis.corda.testacles.mocknetwork.jupiter.MockNetworkExtension
import com.github.manosbatsis.corda.testacles.mocknetwork.jupiter.MockNetworkExtensionConfig
import com.github.manosbatsis.partiture.flow.PartitureFlow
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.StartedMockNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import testacles.sample.cordapp.SampleCordapp
import testacles.sample.cordapp.workflow.YoDto
import testacles.sample.cordapp.workflow.YoFlow1


/** Sample test using [MockNetworkExtension] */
@ExtendWith(MockNetworkExtension::class)
@Tag("mocknetwork")
class MockNetworkExtensionTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(MockNetworkExtensionTest::class.java)

        // Marks the field
        // as a config for the extension
        @MockNetworkExtensionConfig
        @JvmStatic
        val mockNetworkConfig: MockNetworkConfig =
                MockNetworkConfig (
                        // The nodes to build, one of
                        // names: List<MockNodeParameters>, CordaX500Names, OrgNames or
                        // numberOfNodes: Int
                        names = CordaX500Names(listOf(
                                CordaX500Name.parse("O=PartyA, L=Athens, C=GR"),
                                CordaX500Name.parse("O=PartyB, L=Athens, C=GR"))),
                        // Optional, used *only* for the current
                        // Gradle module, if a cordapp.
                        cordappProjectPackage = SampleCordapp::class.java.`package`.name,
                        // Optional; package names are used to pickup
                        // cordapp or cordaCompile dependencies
                        cordappPackages = listOf<String>(PartitureFlow::class.java.`package`.name),
                        // Optional, default
                        threadPerNode = true,
                        // Optional, default
                        networkParameters = testNetworkParameters(
                                minimumPlatformVersion = 1),
                        clearEnv = true)
    }

    // The extension implements a ParameterResolver
    // for NodeHandles
    @Test
    fun `Can retrieve node identity`(nodeHandles: NodeHandles) {
        val nodeA: StartedMockNode = nodeHandles.getNode("partya")
        assertTrue(nodeA.info.legalIdentities.isNotEmpty())
    }

    @Test
    fun `Can send a yo`(nodeHandles: NodeHandles) {
        val nodeA = nodeHandles.getNode("partya")
        val nodeB = nodeHandles.getNode("partyb")
        val yoDto = YoDto(
                recipient = nodeB.info.legalIdentities.first().name,
                message = "Yo from A to B!")
        val yoState = nodeA.startFlow(YoFlow1(yoDto)).getOrThrow()
        assertEquals(yoDto.message, yoState.yo)
        assertEquals(yoDto.recipient, yoState.recipient.name)

    }
}
