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

import com.github.manosbatsis.corda.testacles.containers.config.NodeContainerConfig
import com.github.manosbatsis.corda.testacles.containers.config.database.CordformDatabaseSettings
import com.github.manosbatsis.corda.testacles.containers.config.database.CordformDatabaseSettingsFactory
import com.github.manosbatsis.corda.testacles.containers.cordform.config.CordaNetworkConfig
import com.github.manosbatsis.corda.testacles.containers.cordform.config.CordformNetworkConfig
import com.github.manosbatsis.corda.testacles.containers.util.disableTomcatURLStreamHandlerFactory
import net.corda.core.identity.CordaX500Name
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Container
import org.testcontainers.containers.Network
import org.testcontainers.lifecycle.Startable
import org.testcontainers.utility.DockerImageName
import java.io.File


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

        init {
            disableTomcatURLStreamHandlerFactory()
        }
    }


    @Suppress(names = ["MemberVisibilityCanBePrivate"])
    lateinit var nodes: Map<String, CordformNodeContainer>

    constructor(
            nodesDir: File,
            network: Network = Network.newNetwork(),
            imageName: String,
            imageCordaArgs: String = CordformNetworkConfig.EMPTY,
            databaseSettings: CordformDatabaseSettings = CordformDatabaseSettingsFactory.H2,
            cloneNodesDir: Boolean = false,
            privilegedMode: Boolean = false
    ) : this(CordformNetworkConfig(
                nodesDir = if (cloneNodesDir) CordformNetworkConfig.cloneNodesDir(nodesDir) else nodesDir,
                privilegedMode = privilegedMode,
                databaseSettings = databaseSettings,
                network = network,
                imageName = imageName,
                imageCordaArgs = imageCordaArgs))

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
        addNodeFiles(nodeContainerConfig)
        return CordformNodeContainer(
                dockerImageName = DockerImageName.parse(nodeContainerConfig.imageName),
                nodeContainerConfig = nodeContainerConfig)
                .withPrivilegedMode(cordformNetworkConfig.privilegedMode)
    }

    /** Add "standard" static files that might be needed by the node */
    private fun addNodeFiles(nodeContainerConfig: NodeContainerConfig) {
        // CE and OS 4.6+ need to run-migrations before normal startup
        listOf("cordform/run-corda-after-migrations-pre-4_6.sh", "cordform/run-corda-after-migrations-4_6.sh")
                .forEach {rsPath ->
                    CordformNetworkContainer::class.java.classLoader
                            .getResourceAsStream(rsPath)
                            .use {
                                val targetFile = File(nodeContainerConfig.nodeDir, rsPath.substringAfter('/'))
                                targetFile.writeBytes(it.readBytes())
                                targetFile.setExecutable(true, false)
                            }
                }


    }

    /** Start the network */
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
