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
import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig.Companion.BASE_VERSION_4_5
import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig.Companion.BASE_VERSION_4_6
import com.github.manosbatsis.corda.testacles.containers.config.database.CordformDatabaseSettings
import com.github.manosbatsis.corda.testacles.containers.config.database.CordformDatabaseSettingsFactory
import com.github.manosbatsis.corda.testacles.containers.config.database.DatabaseConfigContributor
import com.github.manosbatsis.corda.testacles.containers.config.network.NetworkConfigContributor
import com.github.manosbatsis.corda.testacles.containers.util.Version
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Network
import java.io.File
import java.util.UUID

/**
 * Utility wrapper of a _nodes_ directory,
 * i.e. one created by the `Cordform` Gradle plugin
 */
data class CordformNetworkConfig(
        override val nodesDir: File,
        override val network: Network,
        override val imageName: String,
        override val imageCordaArgs: String = EMPTY,
        override val entryPointOverride: List<String> = buildEntryPointOverride(imageName),
        override val netParamsFile: File = File(nodesDir, "network-parameters"),
        override val nodeInfosDir: File = File(nodesDir, "additional-node-infos").apply { mkdirs() },
        override val databaseSettings: CordformDatabaseSettings =
                CordformDatabaseSettingsFactory.H2,
        override val privilegedMode: Boolean = false
) : CordaNetworkConfig {
    companion object {
        private val logger = LoggerFactory.getLogger(CordformNetworkConfig::class.java)
        private const val ENTERPRISE = "enterprise"
        const val EMPTY = ""
        const val ENTRYPOINT_WITH_MIGRATIONS_FIRST_4_6 = "/etc/corda/run-corda-after-migrations-4_6.sh"
        const val ENTRYPOINT_WITH_MIGRATIONS_FIRST_CE_PRE_4_6 = "/etc/corda/run-corda-after-migrations-pre-4_6.sh"
        val ENTRYPOINT_MIGRATIONS_FLAGS = listOf("-c", "-a")


        /**
         * Return the default custom entrypoint based on Corda version.
         * For Corda CE or OS 4.6+, a custom entry point will be returned
         * that runs DB migrations before normal node startup.
         */
        fun buildEntryPointOverride(imageName: String): List<String>{
            return imageName?.split("-")?.toMutableList() ?.run {
                if(this.last().toLowerCase() == "snapshot") removeAt(this.size - 1)
                val version = Version(this.last())
                // CE 4.5 or any 4.6+ need DB migrations run first
                val isEnterprise = imageName.toLowerCase().contains(ENTERPRISE)
                val is4p6OrGreater = version >= BASE_VERSION_4_6
                val is4p5 = version == BASE_VERSION_4_5
                when {
                    is4p6OrGreater || (is4p5 && isEnterprise)->
                        listOf(ENTRYPOINT_WITH_MIGRATIONS_FIRST_4_6)
                    //isEnterprise && !is4p5OrGreater ->
                    //    entryPoint.add(ENTRYPOINT_WITH_MIGRATIONS_FIRST_CE_PRE_4_6)
                    else -> emptyList()
                }

            }
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
                            .buildDatabaseSettings(nodeHostName, this)

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
                                            dbSetings.databaseProperties)),
                            entryPointOverride = buildEntryPointOverride(imageName))
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