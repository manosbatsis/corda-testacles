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

import com.github.dockerjava.api.model.ExposedPort
import com.github.manosbatsis.corbeans.test.containers.KImageNameContainer
import com.github.manosbatsis.corbeans.test.containers.SimpleNodeConfig
import com.github.manosbatsis.corda.testacles.jupiter.support.Startables
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigFactory.empty
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueFactory
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.node.services.config.NodeRpcSettings
import net.corda.nodeapi.internal.config.UnknownConfigKeysPolicy
import net.corda.nodeapi.internal.config.parseAs
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.testcontainers.containers.BindMode.READ_WRITE
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.lifecycle.Startable
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.io.FileFilter
import java.nio.file.Path
import java.time.Duration

fun <T> valueFor(any: T): ConfigValue = ConfigValueFactory.fromAnyRef(any)

class ModifiedOnlyFileFilter(
        val sourceDirPath: Path,
        val destDirPath: Path
): FileFilter{
    constructor(sourceDir: File, destDir: File):
            this(sourceDirPath = sourceDir.toPath(), destDirPath = destDir.toPath())

    override fun accept(pathname: File): Boolean {
        val relativePath = sourceDirPath.relativize(pathname.toPath())
        val targetFile = destDirPath.resolve(relativePath).toFile()
        return when{
            !targetFile.exists() -> true
            pathname.lastModified() > targetFile.lastModified() -> true
            else -> false
        }
    }
}

open class CordformContainers private constructor(
        @Suppress("MemberVisibilityCanBePrivate")
        val fsBindDirs: FsBindDirs,
        @Suppress("MemberVisibilityCanBePrivate")
        val network: Network
) : Startables {

    companion object {
        private val logger = LoggerFactory.getLogger(CordformContainers::class.java)
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
    val instances: Map<String, Pair<KImageNameContainer, Config>>

    override val startables: List<Startable>
        get() = instances.map { it.value.first }

    constructor(
            nodesDir: File, network: Network = Network.newNetwork()
    ): this(FsBindDirs(nodesDir = cloneNodesDir(nodesDir, network)), network)

    init {
        instances = fsBindDirs.nodeDirs.map { nodeDir ->
            buildContainer(fsBindDirs, nodeDir, network)
        }.toMap()
    }

    private fun buildCustomNodeConfFile(nodeDir: File): File {
        val rpcSettings =  NodeRpcSettings(
                address = NetworkHostAndPort("0.0.0.0", 10201),
                adminAddress = NetworkHostAndPort("0.0.0.0", 10241),
                ssl = null,
                useSsl = false)
        val origNodeConfFile = File(nodeDir, "node.conf")
        val testaclesNodeConfFile = File(nodeDir, "testacles-node.conf")

        // If not up to date
        if(!testaclesNodeConfFile.exists()
                && testaclesNodeConfFile.lastModified() < origNodeConfFile.lastModified()){

            val config = ConfigFactory.parseFile(origNodeConfFile)
            val rpcSettingsConfig: ConfigObject = empty()
                    .withValue("address", valueFor(rpcSettings.address.toString()))
                    .withValue("adminAddress", valueFor(rpcSettings.adminAddress.toString())).root()
            val testaclesConfig = config
                    .withValue("rpcSettings", rpcSettingsConfig)
                    .withValue("p2pAddress",valueFor("localhost:10200"))
            val testaclesConfigString = testaclesConfig.root()
                    .render(ConfigRenderOptions.concise().setFormatted(true).setJson(false))
            logger.info("getCustomNodeConfFile, config string: ${testaclesConfigString}")
            logger.info("getCustomNodeConfFile, writing to file: ${testaclesNodeConfFile.absolutePath}")
            testaclesNodeConfFile.writeText(testaclesConfigString)

        }
        return testaclesNodeConfFile
    }

    open fun buildContainer(
            fsBindDirs: FsBindDirs, nodeDir: File, network: Network
    ): Pair<String, Pair<KImageNameContainer, Config>> {

        val nodeName = nodeDir.name.decapitalize()
        logger.info("buildContainer, node: ${nodeName}")
        val nodeConfFile = buildCustomNodeConfFile(nodeDir)
        logger.info("buildContainer, parseFile, " +
                "file: ${nodeConfFile.absolutePath}, " +
                "config: ${nodeConfFile.readText()}")
        val config = ConfigFactory.parseFile(nodeConfFile)
        //val cordappConf = TypesafeCordappConfig(config)
        val nodeConfig = config.parseAs<SimpleNodeConfig>(UnknownConfigKeysPolicy.IGNORE::handle)

        val rpcPort = nodeConfig.rpcSettings.address!!.port
        val exposedPorts = listOf(rpcPort,
                nodeConfig.rpcSettings.adminAddress!!.port,
                nodeConfig.p2pAddress.port)
        val instance = KImageNameContainer(
                DockerImageName.parse("corda/corda-zulu-java1.8-4.5"))
                .withPrivilegedMode(true)
                .withNetworkAliases(nodeName)
                .withNetwork(network)
                .withExposedPorts(*exposedPorts.toTypedArray())
                .withFileSystemBind(
                        nodeDir.absolutePath, "/etc/corda",
                        READ_WRITE)
                .withFileSystemBind(
                        nodeConfFile.absolutePath, "/etc/corda/node.conf",
                        READ_WRITE)
                .withFileSystemBind(
                        nodeDir.resolve("certificates").absolutePath,
                        "/opt/corda/certificates",
                        READ_WRITE)
                .withFileSystemBind(
                        nodeDir.absolutePath,
                        "/opt/corda/persistence",
                        READ_WRITE)
                .withFileSystemBind(
                        nodeDir.resolve("logs").absolutePath,
                        "/opt/corda/logs",
                        READ_WRITE)
                .withFileSystemBind(
                        fsBindDirs.netParamsFile.absolutePath,
                        "/opt/corda/network-parameters",
                        READ_WRITE)
                .withFileSystemBind(
                        fsBindDirs.nodeInfosDir.absolutePath,
                        "/opt/corda/additional-node-infos",
                        READ_WRITE)
                .withCreateContainerCmdModifier { cmd ->
                    cmd.withHostName(nodeName)
                            .withName(nodeName)
                            .withExposedPorts(exposedPorts.map { port ->
                                ExposedPort.tcp(port)
                            })
                }
                .withLogConsumer {
                    logger.info(it.utf8String)
                }
                .waitingFor(Wait.forLogMessage(".*started up and registered in.*", 1))
                .withStartupTimeout(Duration.ofMinutes(2))
        /*logger.info("started container, \n" +
                "host: ${instance.host}, \n" +
                "address: ${instance.containerIpAddress}, \n" +
                "exposedPorts: ${instance.exposedPorts}, \n" +
                "portBindings: ${instance.portBindings}, \n" +
                "boundPortNumbers: ${instance.boundPortNumbers} \n" +
                "info: ${instance.containerInfo}")
        */
        return nodeName to Pair(instance, config)
        //if(nodeConfig.notary == null) {
        //    logger.info("Note non-notary port mapping for : ${nodeName}")
        //    instancePorts[nodeName] = instance.getMappedPort(rpcPort)
        //}
    }


}
