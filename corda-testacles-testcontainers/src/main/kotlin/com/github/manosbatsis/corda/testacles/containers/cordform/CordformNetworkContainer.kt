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
import com.github.manosbatsis.corda.testacles.containers.cordform.fs.CordformNodesFs
import com.github.manosbatsis.corda.testacles.containers.cordform.fs.NodeLocalFs
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
open class CordformNetworkContainer private constructor(
        @Suppress(names = ["MemberVisibilityCanBePrivate"])
        val cordformNodesFs: CordformNodesFs,
        @Suppress(names = ["MemberVisibilityCanBePrivate"])
        val network: Network,
        @Suppress(names = ["MemberVisibilityCanBePrivate"])
        val imageName: DockerImageName = DEFAULT_CORDA_IMAGE_NAME_4_5

) : Startable {

    companion object {
        private val logger = LoggerFactory.getLogger(CordformNetworkContainer::class.java)
        val DEFAULT_CORDA_IMAGE_NAME_4_5: DockerImageName = DockerImageName.parse(
                "corda/corda-zulu-java1.8-4.5")
        init {
            disabpleTomcatURLStreamHandlerFactory()
        }
    }

    @Suppress(names = ["MemberVisibilityCanBePrivate"])
    val nodes: Map<String, CordformNodeContainer> by lazy {
        cordformNodesFs.nodeFsList.map { nodeDir ->
            val container = buildContainer(nodeDir, network)
            container.nodeName to container
        }.toMap()
    }

    constructor(
            nodesDir: File,
            network: Network = Network.newNetwork(),
            imageName: DockerImageName = DEFAULT_CORDA_IMAGE_NAME_4_5,
            cloneNodesDir: Boolean = false
    ) : this(CordformNodesFs(if (cloneNodesDir) CordformNodesFs.cloneNodesDir(nodesDir) else nodesDir),
            network, imageName)

    fun getNode(nodeIdentity: CordaX500Name): CordformNodeContainer {
        return nodes.values.find { it.nodeIdentity == nodeIdentity }
                ?: error("Node not found for identity: $nodeIdentity")
    }

    fun getNode(nodeName: String): CordformNodeContainer {
        return nodes[nodeName] ?: error("Node not found for name: $nodeName")
    }

    protected fun buildContainer(
            nodeLocalFs: NodeLocalFs, network: Network
    ): CordformNodeContainer {
        return CordformNodeContainer(
                dockerImageName = imageName,
                nodeLocalFs = nodeLocalFs)
                .withPrivilegedMode(true)
                .withNetwork(network)
                .withLogConsumer {
                    logger.info(it.utf8String)
                }
                .waitingFor(Wait.forLogMessage(".*started up and registered in.*", 1))
                .withStartupTimeout(Duration.ofMinutes(2))
    }

    override fun start() {
        this.nodes.forEach { it.value.start() }
    }

    override fun stop() {
        this.nodes.forEach { it.value.stop() }
    }

}
