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
import com.github.manosbatsis.partiture.flow.PartitureFlow
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.StartedMockNode
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import testacles.sample.cordapp.SampleCordapp
import testacles.sample.cordapp.workflow.YoDto
import testacles.sample.cordapp.workflow.YoFlow1

/** Sample test using the [MockNetworkHelper] directly */
@Tag("mocknetwork")
class MockNetworkHelperStaticTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(MockNetworkHelperStaticTest::class.java)

        @JvmStatic
        val nodesHelper: MockNetworkHelper by lazy {
            MockNetworkHelper(MockNetworkConfig (
                    // The nodes to build, one of
                    // names: List<MockNodeParameters>, CordaX500Names, OrgNames or
                    // numberOfNodes: Int
                    numberOfNodes = 2,
                    // Package names, one per cordapp to pickup
                    cordappPackages = listOf<String>(
                            SampleCordapp::class.java.`package`.name,
                            PartitureFlow::class.java.`package`.name),
                    // Optional, default
                    threadPerNode = true,
                    // Optional, default
                    networkParameters = testNetworkParameters(
                            minimumPlatformVersion = 1)))
        }

        /** Start the Corda MockNetwork network */
        @JvmStatic
        @BeforeAll
        fun beforeAll() { nodesHelper.start() }

        /** Stop the Corda network */
        @JvmStatic
        @AfterAll
        fun afterAll() { nodesHelper.stop() }

    }

    @Test
    fun `Can retrieve node identity`() {
        val nodeA: StartedMockNode = nodesHelper.nodeHandles
                .getNode("party1")
        assertTrue(nodeA.info.legalIdentities.isNotEmpty())
    }

    @Test
    fun `Can send a yo`() {
        val nodeA = nodesHelper.nodeHandles.getNode("party1")
        val nodeB = nodesHelper.nodeHandles.getNode("party2")
        val yoDto = YoDto(
                recipient = nodeB.info.legalIdentities.first().name,
                message = "Yo from Party1 to Party2!")
        val yoState = nodeA.startFlow(YoFlow1(yoDto)).getOrThrow()
        assertEquals(yoDto.message, yoState.yo)
        assertEquals(yoDto.recipient, yoState.recipient.name)

    }
}
