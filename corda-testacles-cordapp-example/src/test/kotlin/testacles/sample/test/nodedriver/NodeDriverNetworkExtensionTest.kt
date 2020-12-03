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
package testacles.sample.test.nodedriver

import com.github.manosbatsis.corda.testacles.nodedriver.NodeHandles
import com.github.manosbatsis.corda.testacles.nodedriver.config.NodeDriverNodesConfig
import com.github.manosbatsis.corda.testacles.nodedriver.jupiter.NodeDriverExtensionConfig
import com.github.manosbatsis.corda.testacles.nodedriver.jupiter.NodeDriverNetworkExtension
import net.corda.core.utilities.getOrThrow
import net.corda.testing.driver.NodeHandle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import testacles.sample.cordapp.workflow.YoDto
import testacles.sample.cordapp.workflow.YoFlow1

/** Sample test using [NodeDriverNetworkExtension] */
@ExtendWith(NodeDriverNetworkExtension::class)
@Tag("nodedriver")
class NodeDriverNetworkExtensionTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(NodeDriverNetworkExtensionTest::class.java)

        // Marks the field
        // as a config for the extension
        @NodeDriverExtensionConfig
        @JvmStatic
        val nodeDriverConfig: NodeDriverNodesConfig =
                TestConfigUtil.myCustomNodeDriverConfig()
    }

    // The extension implements a ParameterResolver
    // for NodeHandles
    @Test
    fun `Can retrieve node identity`(nodeHandles: NodeHandles) {
        val nodeA: NodeHandle = nodeHandles.getNode("partya")
        assertTrue(nodeA.nodeInfo.legalIdentities.isNotEmpty())
    }

    @Test
    fun `Can send a yo`(nodeHandles: NodeHandles) {
        val nodeA = nodeHandles.getNode("partya")
        val nodeB = nodeHandles.getNode("partyb")
        val yoDto = YoDto(
                recipient = nodeB.nodeInfo.legalIdentities.first().name,
                message = "Yo from A to B!")
        val yoState = nodeA.rpc.startFlowDynamic(YoFlow1::class.java, yoDto)
                .returnValue.getOrThrow()
        assertEquals(yoDto.message, yoState.yo)
        assertEquals(yoDto.recipient, yoState.recipient.name)

    }
}
