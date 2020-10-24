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
package com.github.manosbatsis.corda.testacles.containers.cordform

import com.github.manosbatsis.corbeans.test.containers.disabpleTomcatURLStreamHandlerFactory
import com.github.manosbatsis.corda.testacles.containers.config.NodeContainerConfig
import com.github.manosbatsis.corda.testacles.containers.cordform.config.CordaNetworkConfig
import com.github.manosbatsis.corda.testacles.containers.cordform.config.CordformNetworkConfig
import net.corda.core.identity.CordaX500Name
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Container
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.lifecycle.Startable
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.time.Duration


/**
 * Wraps a set of [Container]s composing a Corda Network,
 * using the output of the `Cordform` Gradle plugin as source.
 */
open class CordformNetworkContainer(
        @Suppress(names = ["MemberVisibilityCanBePrivate"])
        val cordformNetworkConfig: CordaNetworkConfig
) : Startable {

    companion object {
        private val logger = LoggerFactory.getLogger(CordformNetworkContainer::class.java)
        val CORDA_IMAGE_NAME_4_5: DockerImageName = DockerImageName.parse(
                "corda/corda-zulu-java1.8-4.5")
        val CORDA_IMAGE_NAME_4_6: DockerImageName = DockerImageName.parse(
                "corda/corda-zulu-java1.8-4.6")
        init {
            disabpleTomcatURLStreamHandlerFactory()
        }
    }

    @Suppress(names = ["MemberVisibilityCanBePrivate"])
    lateinit var nodes: Map<String, CordformNodeContainer>

    constructor(
            nodesDir: File,
            network: Network = Network.newNetwork(),
            imageName: DockerImageName = CORDA_IMAGE_NAME_4_5,
            databaseSettings: CordformDatabaseSettings =
                    CordformDatabaseSettingsFactory.H2,
            cloneNodesDir: Boolean = false,
            privilegedMode: Boolean = false
    ) : this(CordformNetworkConfig(
                nodesDir = if (cloneNodesDir) CordformNetworkConfig.cloneNodesDir(nodesDir) else nodesDir,
                privilegedMode = privilegedMode,
                databaseSettings = databaseSettings,
                network = network,
                imageName = imageName))

    fun getNode(nodeIdentity: CordaX500Name): CordformNodeContainer {
        return nodes.values.find { it.nodeIdentity == nodeIdentity }
                ?: error("Node not found for identity: $nodeIdentity")
    }

    fun getNode(nodeName: String): CordformNodeContainer {
        return nodes[nodeName] ?: error("Node not found for name: $nodeName")
    }

    protected fun buildContainer(
            nodeContainerConfig: NodeContainerConfig
    ): CordformNodeContainer {
        return CordformNodeContainer(
                dockerImageName = nodeContainerConfig.imageName,
                nodeContainerConfig = nodeContainerConfig)
                .withPrivilegedMode(cordformNetworkConfig.privilegedMode)
                .withNetwork(nodeContainerConfig.network)
                .withLogConsumer {
                    logger.info(it.utf8String)
                }
                .waitingFor(Wait.forLogMessage(".*started up and registered in.*", 1))
                .withStartupTimeout(Duration.ofMinutes(2))
    }

    override fun start() {
        nodes = cordformNetworkConfig.nodeConfigs
                // Create and start node containers
                .map { nodeLocalFs ->
                    val container = buildContainer(nodeLocalFs)
                    container.start()
                    container.nodeName to container
                }.toMap()
    }

    override fun stop() {
        // Stop node containers
        this.nodes.forEach { it.value.stop() }
    }

}
