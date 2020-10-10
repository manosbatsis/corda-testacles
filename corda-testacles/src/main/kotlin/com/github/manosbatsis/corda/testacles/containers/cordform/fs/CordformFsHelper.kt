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
package com.github.manosbatsis.corda.testacles.containers.cordform.fs

import com.github.manosbatsis.corda.testacles.containers.NodeContainer.Companion.P2P_PORT
import com.github.manosbatsis.corda.testacles.containers.NodeContainer.Companion.RPC_ADMIN_PORT
import com.github.manosbatsis.corda.testacles.containers.NodeContainer.Companion.RPC_HOST
import com.github.manosbatsis.corda.testacles.containers.NodeContainer.Companion.RPC_PORT
import com.github.manosbatsis.corda.testacles.containers.cordform.valueFor
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigRenderOptions
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.node.services.config.NodeRpcSettings
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Utility wrapper of a _nodes_ directory,
 * i.e. one created by the `Cordform` Gradle plugin
 */
data class CordformFsHelper(
        val nodesDir: File,
        val netParamsFile: File = File(nodesDir, "network-parameters"),
        val nodeInfosDir: File = File(nodesDir, "additional-node-infos").apply { mkdirs() }
){
    companion object{
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
            val rpcSettings =  NodeRpcSettings(
                    address = NetworkHostAndPort(RPC_HOST, RPC_PORT),
                    adminAddress = NetworkHostAndPort(RPC_HOST, RPC_ADMIN_PORT),
                    ssl = null,
                    useSsl = false)
            val origNodeConfFile = File(nodeDir, "node.conf")
            val testaclesNodeConfFile = File(nodeDir, "testacles-node.conf")

            // If not up to date, customize config
            if(!testaclesNodeConfFile.exists()
                    && testaclesNodeConfFile.lastModified() < origNodeConfFile.lastModified()){

                val config = ConfigFactory.parseFile(origNodeConfFile)
                val rpcSettingsConfig: ConfigObject = ConfigFactory.empty()
                        .withValue("address", valueFor(rpcSettings.address.toString()))
                        .withValue("adminAddress", valueFor(rpcSettings.adminAddress.toString())).root()
                val testaclesConfig = config
                        .withValue("rpcSettings", rpcSettingsConfig)
                        .withValue("p2pAddress", valueFor("localhost:$P2P_PORT"))
                val testaclesConfigString = testaclesConfig.root()
                        .render(ConfigRenderOptions.concise().setFormatted(true).setJson(false))
                testaclesNodeConfFile.writeText(testaclesConfigString)

            }
            return testaclesNodeConfFile
        }
    }

    private val allSubDirs = nodesDir.listFiles { file ->
        file.isDirectory && File(file, "node.conf").exists()
    }.takeIf { it.isNotEmpty() } ?: error("Could not find any node directories in ${nodesDir.absolutePath}")

    val notaryNodeDirs: List<File>
        get() = allSubDirs.filter { it.name.contains("notary", true) }

    val partyNodeDirs: List<File>
        get() = allSubDirs.filter { !it.name.contains("notary", true) }

    val nodeLocalves: List<NodeLocalFs>
        get() = (notaryNodeDirs + partyNodeDirs)
                .map { nodeDir ->
                    NodeLocalFs(
                        nodeDir = nodeDir,
                        nodeConfFile = buildCustomNodeConfFile(nodeDir),
                        netParamsFile = netParamsFile,
                        nodeInfosDir = nodeInfosDir) }

    init {
        FileUtils.copyFile(
                File(nodeLocalves.first().nodeDir, "network-parameters"),
                netParamsFile)
    }
}