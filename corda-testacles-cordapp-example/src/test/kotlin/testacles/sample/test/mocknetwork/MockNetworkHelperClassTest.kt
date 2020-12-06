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
package testacles.sample.test.mocknetwork

import com.github.manosbatsis.corda.testacles.mocknetwork.MockNetworkHelper
import com.github.manosbatsis.corda.testacles.mocknetwork.config.MockNetworkConfig
import com.github.manosbatsis.corda.testacles.mocknetwork.config.OrgNames
import com.github.manosbatsis.partiture.flow.PartitureFlow
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.StartedMockNode
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.slf4j.LoggerFactory
import testacles.sample.cordapp.SampleCordapp
import testacles.sample.cordapp.workflow.YoDto
import testacles.sample.cordapp.workflow.YoFlow1

/** Sample class lifecycle test using the [MockNetworkHelper] directly */
@TestInstance(PER_CLASS)
@Tag("mocknetwork")
@Disabled
class MockNetworkHelperClassTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(MockNetworkHelperClassTest::class.java)
    }

    val nodesHelper: MockNetworkHelper by lazy {
        MockNetworkHelper(MockNetworkConfig (
                // The nodes to build, one of
                // names: List<MockNodeParameters>, CordaX500Names, OrgNames or
                // numberOfNodes: Int
                names = OrgNames(listOf("PartyA", "PartyB")),
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
                // Optional, default is false.
                // Useful in some CI environments
                clearEnv = true))
    }

    /** Start the Corda MockNetwork network */
    @BeforeAll
    fun beforeAll() { nodesHelper.start() }

    /** Stop the Corda network */
    @AfterAll
    fun afterAll() { nodesHelper.stop() }

    @Test
    fun `Can retrieve node identity`() {
        val nodeA: StartedMockNode = nodesHelper.nodeHandles
                .getNode("partya")
        assertTrue(nodeA.info.legalIdentities.isNotEmpty())
    }

    @Test
    fun `Can send a yo`() {
        val nodeA = nodesHelper.nodeHandles.getNode("partya")
        val nodeB = nodesHelper.nodeHandles.getNode("partyb")
        val yoDto = YoDto(
                recipient = nodeB.info.legalIdentities.first().name,
                message = "Yo from A to B!")
        val yoState = nodeA.startFlow(YoFlow1(yoDto)).getOrThrow()
        assertEquals(yoDto.message, yoState.yo)
        assertEquals(yoDto.recipient, yoState.recipient.name)

    }
}
