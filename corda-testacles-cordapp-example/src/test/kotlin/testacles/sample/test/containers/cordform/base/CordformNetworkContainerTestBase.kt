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
package testacles.sample.test.containers.cordform.base

import com.github.manosbatsis.corda.testacles.containers.config.database.CordformDatabaseSettingsFactory.POSTGRES
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNetworkContainer
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNodeContainer
import net.corda.core.utilities.getOrThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.testcontainers.junit.jupiter.Testcontainers
import testacles.sample.cordapp.workflow.YoDto
import testacles.sample.cordapp.workflow.YoFlow1
import java.io.File


/** An RPC-based test using [CordformNetworkContainer] */
@Testcontainers
@Tag("cordform")
abstract class CordformNetworkContainerTestBase {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CordformNetworkContainerTestBase::class.java)

        @JvmStatic
        fun createCordformNetworkContainer(
                dockerImageName: String
        ): CordformNetworkContainer {
            return CordformNetworkContainer(
                    imageName =  dockerImageName,
                    nodesDir = File(System.getProperty("user.dir"))
                            .parentFile.resolve("build/nodes"),
                    // Will clone nodesDir to build/testacles/{random UUID}
                    // and use that instead
                    cloneNodesDir = true,
                    privilegedMode = false,
                    // Create a Postgres DB for each node (default is H2)
                    databaseSettings = POSTGRES)
        }
    }

    abstract fun getCordformNetworkContainer(): CordformNetworkContainer

    @Test
    fun `Can retrieve node identity`() {
        val nodeA: CordformNodeContainer = getCordformNetworkContainer().nodes["partya"]
                ?: error("Instance not found")
        assertTrue(nodeA.nodeIdentity.toString().contains("PartyA"))
    }

    @Test
    fun `Can send a yo`() {
        val nodeA = getCordformNetworkContainer().getNode("partya")
        val nodeB = getCordformNetworkContainer().getNode("partyb")
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
