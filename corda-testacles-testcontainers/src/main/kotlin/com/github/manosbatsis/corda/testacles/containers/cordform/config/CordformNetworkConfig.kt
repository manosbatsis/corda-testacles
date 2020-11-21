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
package com.github.manosbatsis.corda.testacles.containers.cordform.config

import com.github.manosbatsis.corda.testacles.containers.config.NodeContainerConfig
import com.github.manosbatsis.corda.testacles.containers.config.database.DatabaseConfigContributor
import com.github.manosbatsis.corda.testacles.containers.config.network.NetworkConfigContributor
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformDatabaseSettings
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformDatabaseSettingsFactory
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNetworkContainer
import org.apache.commons.io.FileUtils
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.util.UUID

/**
 * Utility wrapper of a _nodes_ directory,
 * i.e. one created by the `Cordform` Gradle plugin
 */
data class CordformNetworkConfig(
        override val nodesDir: File,
        override val network: Network,
        override val imageName: DockerImageName,
        override val imageCordaArgs: String = EMPTY,
        override val entryPointOverride: List<String> = getEntryPoint(imageName),
        override val netParamsFile: File = File(nodesDir, "network-parameters"),
        override val nodeInfosDir: File = File(nodesDir, "additional-node-infos").apply { mkdirs() },
        override val databaseSettings: CordformDatabaseSettings =
                CordformDatabaseSettingsFactory.H2,
        override val privilegedMode: Boolean = false
) : CordaNetworkConfig {
    companion object {

        const val EMPTY = ""

        val customImageEntryPoints: Map<DockerImageName, String?> = mapOf(
                CordformNetworkContainer.CORDA_IMAGE_NAME_4_6 to "/etc/corda/run-corda-after-migrations.sh"
        )

        private fun getEntryPoint(imageName: DockerImageName): List<String>{
            val entryPoint = customImageEntryPoints[imageName]
            return entryPoint?.split(" ")?.toList() ?: emptyList()
        }

        private fun toNodeHostName(nodeDir: File) = nodeDir.name
                .replace(" ", "_").toLowerCase()

        /**
         * Clone the original source to _build/testacles_.
         */
        fun cloneNodesDir(nodesDir: File): File {
            val projectDir = File(System.getProperty("user.dir"))
            val buildDir = File(projectDir, "build")
            val testaclesDir = File(buildDir, "testacles")
            val testacleDir = File(testaclesDir, UUID.randomUUID().toString())
            testacleDir.mkdirs()
            FileUtils.copyDirectory(
                    nodesDir, testacleDir,
                    ModifiedOnlyFileFilter(nodesDir, testacleDir), false)
            return testacleDir
        }
    }


    private val nodeDirs: Array<File> = nodesDir.listFiles { file ->
        file.isDirectory && File(file, "node.conf").exists()
    }.takeIf { it.isNotEmpty() } ?: error("Could not find any node directories in ${nodesDir.absolutePath}")

    override val notaryNodeDirs: List<File> by lazy {
        nodeDirs.filter { it.name.contains("notary", true) }
    }

    override val partyNodeDirs: List<File> by lazy {
        nodeDirs.filter { !it.name.contains("notary", true) }
    }

    override val nodeConfigs: List<NodeContainerConfig> by lazy {
        (notaryNodeDirs + partyNodeDirs)
                .map { nodeDir ->
                    val nodeHostName = toNodeHostName(nodeDir)
                    val dbSetings = databaseSettings
                            .buildDatabaseSettings(nodeHostName, network)
                    NodeContainerConfig(
                            nodeDir = nodeDir,
                            imageName = imageName,
                            imageCordaArgs = imageCordaArgs,
                            network = network,
                            nodeHostName = nodeHostName,
                            netParamsFile = netParamsFile,
                            nodeInfosDir = nodeInfosDir,
                            configContributors = listOf(
                                    NetworkConfigContributor(nodeHostName),
                                    DatabaseConfigContributor(
                                            dbSetings.databaseConnectionProperties,
                                            dbSetings.databaseProperties)
                            ))
                }
    }

    init {
        if (!nodesDir.exists())
            throw IllegalArgumentException("The nodesDir param must point to an existing directory")
        FileUtils.copyFile(
                File(nodeConfigs.first().nodeDir, "network-parameters"),
                netParamsFile)
    }
}