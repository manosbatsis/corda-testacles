/*
 * Corda Testacles: Test containers and tools to help cordapps grow.
 * Copyright (C) 2018 Manos Batsis
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

import com.github.manosbatsis.corda.testacles.containers.CordaImageNameNodeContainer
import com.github.manosbatsis.corda.testacles.containers.cordform.fs.CordformFsHelper
import com.github.manosbatsis.corda.testacles.containers.cordform.fs.ModifiedOnlyFileFilter
import com.github.manosbatsis.corda.testacles.containers.cordform.fs.NodeLocalFs
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueFactory
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Container
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.lifecycle.Startable
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.time.Duration

fun <T> valueFor(any: T): ConfigValue = ConfigValueFactory.fromAnyRef(any)

/**
 * Wraps a set of [Container]s composing a Corda Network,
 * using the output of the `Cordform` Gradle plugin as source.
 */
open class CordformNodesContainer private constructor(
        @Suppress("MemberVisibilityCanBePrivate")
        val cordformFsHelper: CordformFsHelper,
        @Suppress("MemberVisibilityCanBePrivate")
        val network: Network
) : Startable {

    companion object {
        private val logger = LoggerFactory.getLogger(CordformNodesContainer::class.java)

        /**
         * Clone the original source to _build/testacles_.
         */
        fun cloneNodesDir(nodesDir: File, network: Network): File {
            val projectDir = File(System.getProperty("user.dir"))
            val buildDir = File(projectDir, "build")
            val testaclesDir = File(buildDir, "testacles")
            val testacleDir = File(testaclesDir, network.id)
            testacleDir.mkdirs()
            FileUtils.copyDirectory(
                    nodesDir,  testacleDir,
                    ModifiedOnlyFileFilter(nodesDir, testacleDir), false)
            return testacleDir
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val instances: Map<String, CordaImageNameNodeContainer>

    constructor(
            nodesDir: File, network: Network = Network.newNetwork()
    ): this(CordformFsHelper(nodesDir = cloneNodesDir(nodesDir, network)), network)

    init {
        instances = cordformFsHelper.nodeLocalves.map { nodeDir ->
            val container = buildContainer(cordformFsHelper, nodeDir, network)
            container.nodeName to container
        }.toMap()
    }

    open fun buildContainer(
            cordformFsHelper: CordformFsHelper, nodeLocalFs: NodeLocalFs, network: Network
    ): CordaImageNameNodeContainer {
        return CordaImageNameNodeContainer(
                dockerImageName = DockerImageName.parse("corda/corda-zulu-java1.8-4.5"),
                nodeLocalFs = nodeLocalFs)
                .withPrivilegedMode(true)
                .withNetwork(network)
                .withLogConsumer {
                    logger.info(it.utf8String)
                }
                .waitingFor(Wait.forLogMessage(".*started up and registered in.*", 1))
                .withStartupTimeout(Duration.ofMinutes(2))

    }

    override fun start(){
        this.instances.forEach { it.value.start() }
    }

    override fun stop(){
        this.instances.forEach { it.value.stop() }
    }

}
