/*
 * Corda Testacles: Tools to grow some cordapp test suites.
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
package com.github.manosbatsis.corda.testacles.containers.cordform.fs

import com.github.manosbatsis.corda.testacles.containers.cordform.valueFor
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion.NODE_CONF_FILENAME_CUSTOM
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion.NODE_CONF_FILENAME_DEFAULT
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion.P2P_PORT
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion.RPC_ADMIN_PORT
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion.RPC_HOST
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion.RPC_PORT
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigRenderOptions
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.node.services.config.NodeRpcSettings
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.UUID

/**
 * Utility wrapper of a _nodes_ directory,
 * i.e. one created by the `Cordform` Gradle plugin
 */
data class CordformNodesFs(
        val nodesDir: File,
        val netParamsFile: File = File(nodesDir, "network-parameters"),
        val nodeInfosDir: File = File(nodesDir, "additional-node-infos").apply { mkdirs() }
) {
    companion object {

        /**
         * Build a custom node.conf based on the original.
         * The original config file is cloned to _testacles-node.conf_
         * and normalized as follows:
         *
         * - RPC address: [RPC_HOST]:[RPC_PORT]
         * - RPC admin address: [RPC_HOST]:[RPC_ADMIN_PORT]
         * - P2P address: localhost:[P2P_PORT]
         */
        fun buildCustomNodeConfFile(nodeDir: File): File {
            val nodeHostName = toNodeHostName(nodeDir)
            val rpcSettings = NodeRpcSettings(
                    address = NetworkHostAndPort(RPC_HOST, RPC_PORT),
                    adminAddress = NetworkHostAndPort(RPC_HOST, RPC_ADMIN_PORT),
                    ssl = null,
                    useSsl = false)
            val origNodeConfFile = File(nodeDir, "node.conf")
            val testaclesNodeConfFile = File(nodeDir, "testacles-node.conf")

            // If not up to date, customize config
            if (!testaclesNodeConfFile.exists()
                    && testaclesNodeConfFile.lastModified() < origNodeConfFile.lastModified()) {

                val config = ConfigFactory.parseFile(origNodeConfFile)
                val rpcSettingsConfig: ConfigObject = ConfigFactory.empty()
                        .withValue("address", valueFor(rpcSettings.address.toString()))
                        .withValue("adminAddress", valueFor(rpcSettings.adminAddress.toString())).root()
                val testaclesConfig = config
                        .withValue("rpcSettings", rpcSettingsConfig)
                        .withValue("p2pAddress", valueFor("$nodeHostName:$P2P_PORT"))
                val testaclesConfigString = testaclesConfig.root()
                        .render(ConfigRenderOptions.concise().setFormatted(true).setJson(false))
                testaclesNodeConfFile.writeText(testaclesConfigString)

            }
            return testaclesNodeConfFile
        }

        fun toNodeHostName(nodeDir: File) = nodeDir.name
                .replace(" ", "_").toLowerCase()

        /**
         * Looks for a config file in the node directory,
         * first checking for [NODE_CONF_FILENAME_CUSTOM],
         * then falling back to [NODE_CONF_FILENAME_DEFAULT] \
         */
        fun resolveConfig(nodeDir: File): File {
            return listOf(NODE_CONF_FILENAME_CUSTOM, NODE_CONF_FILENAME_DEFAULT)
                    .map { nodeDir.resolve(it) }
                    .find { it.exists() }
                    ?: throw IllegalArgumentException("Input file must be a node.conf or node directory")
        }

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


    val notaryNodeDirs: List<File> by lazy {
        nodeDirs.filter { it.name.contains("notary", true) }
    }

    val partyNodeDirs: List<File> by lazy {
        nodeDirs.filter { !it.name.contains("notary", true) }
    }

    val nodeFsList: List<NodeLocalFs> by lazy {
        (notaryNodeDirs + partyNodeDirs)
                .map { nodeDir ->
                    NodeLocalFs(
                            nodeDir = nodeDir,
                            nodeHostName = toNodeHostName(nodeDir),
                            nodeConfFile = buildCustomNodeConfFile(nodeDir),
                            netParamsFile = netParamsFile,
                            nodeInfosDir = nodeInfosDir)
                }
    }

    init {
        if (!nodesDir.exists())
            throw IllegalArgumentException("The nodesDir param must point to an existing directory")
        FileUtils.copyFile(
                File(nodeFsList.first().nodeDir, "network-parameters"),
                netParamsFile)
    }
}