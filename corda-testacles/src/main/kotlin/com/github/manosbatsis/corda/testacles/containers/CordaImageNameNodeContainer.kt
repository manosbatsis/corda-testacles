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
package com.github.manosbatsis.corda.testacles.containers

import com.github.dockerjava.api.model.ExposedPort
import com.github.manosbatsis.corda.rpc.poolboy.connection.LazyNodeRpcConnection
import com.github.manosbatsis.corda.rpc.poolboy.connection.NodeRpcConnection
import com.github.manosbatsis.corda.rpc.poolboy.pool.connection.NodeRpcConnectionConfig
import com.github.manosbatsis.corda.testacles.containers.cordform.fs.NodeLocalFs
import com.github.manosbatsis.corda.testacles.model.SimpleNodeConfig
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.nodeapi.internal.config.UnknownConfigKeysPolicy.IGNORE
import net.corda.nodeapi.internal.config.User
import net.corda.nodeapi.internal.config.parseAs
import org.slf4j.LoggerFactory
import org.testcontainers.containers.BindMode.READ_WRITE
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.util.function.Consumer

open class CordaImageNameNodeContainer(
        dockerImageName: DockerImageName = DockerImageName.parse("corda/corda-zulu-java1.8-4.5"),
        val nodeLocalFs: NodeLocalFs
): GenericContainer<CordaImageNameNodeContainer>(dockerImageName), NodeContainer {

    companion object{
        private val logger = LoggerFactory.getLogger(CordaImageNameNodeContainer::class.java)
        private val WHITESPACE = "\\s++".toRegex()
    }

    val config: Config by lazy { ConfigFactory.parseFile(nodeLocalFs.nodeConfFile) }

    override val nodeName: String
        get() = nodeIdentity.organisation.replace(WHITESPACE, "").decapitalize()


    override val nodeIdentity: CordaX500Name
        get() = simpleNodeConfig.myLegalName

    override val simpleNodeConfig: SimpleNodeConfig
        get() = config.parseAs(IGNORE::handle)

    override val rpcNetworkHostAndPort: NetworkHostAndPort
        get() = NetworkHostAndPort(host,
                getMappedPort(simpleNodeConfig.rpcSettings.address!!.port))

    override val rpcAddress: String
        get() = rpcNetworkHostAndPort.toString()

    override val rpcUsers: List<out User>
        get() {
            return if (config.hasPath("rpcUsers")) {
                config.getConfigList("rpcUsers")
            } else {
                config.getConfigList("security.authService.dataSource.users")
            }.map { User(
                    username = it.getString("user"),
                    password = it.getString("password"),
                    permissions = it.getStringList("permissions").toSet())
            }
        }

/*
    constructor(
            dockerImageName: DockerImageName,
            nodeConfFile: File,
            nodeDir: File?
    ): this(dockerImageName = dockerImageName,
            config = ConfigFactory.parseFile(nodeConfFile),
            nodeDir = nodeDir){
        addFileSystemBind(nodeConfFile.absolutePath, "/etc/corda/node.conf", READ_WRITE)
    }

    constructor(
            dockerImageName: DockerImageName,
            inputFile: File
    ): this(dockerImageName = dockerImageName,
            config = NodeLocalFs.buildConfig(inputFile),
            nodeDir = if(inputFile.isDirectory) inputFile else null)

    constructor(
            dockerImageName: DockerImageName,
            nodeLocalFs: NodeLocalFs
    ): this(dockerImageName = dockerImageName,
            config = NodeLocalFs.buildConfig(nodeLocalFs.nodeConfFile),
            nodeDir = nodeLocalFs.nodeDir,
            netParamsFile = nodeLocalFs.netParamsFile,
            nodeInfosDir = nodeLocalFs.nodeInfosDir)
*/
    // Initialize network alias, ports, FS binds
    init {
        logger.debug("Initializing, nodeDir: ${nodeLocalFs.nodeDir?.absolutePath}, config: ${config}")
        networkAliases.add(nodeName)
        setupNetwork()
        setupFileSystemBinds()
    }

    override fun createRpcConnection(user: User): NodeRpcConnection {
        val rpcConfig = NodeRpcConnectionConfig(
                address = rpcAddress,
                username = user.username,
                password = user.password,
                eager = false,
                targetLegalIdentity = simpleNodeConfig.myLegalName)
        return LazyNodeRpcConnection(rpcConfig)
    }

    private fun setupFileSystemBinds() {
        with(nodeLocalFs) {
            // node.conf
            addFileSystemBind(nodeConfFile.absolutePath, "/etc/corda/node.conf", READ_WRITE)
            // Corda dir
            nodeDir?.also {nodeDir ->
                addFileSystemBind(nodeDir.absolutePath, "/etc/corda", READ_WRITE)
                addFileSystemBind(nodeDir.resolve("cordapps").absolutePath,
                        "/opt/corda/cordapps", READ_WRITE)
                addFileSystemBind(nodeDir.resolve("certificates").absolutePath,
                        "/opt/corda/certificates", READ_WRITE)
                addFileSystemBind(nodeDir.absolutePath, "/opt/corda/persistence", READ_WRITE)
                addFileSystemBind(nodeDir.resolve("logs").absolutePath,
                        "/opt/corda/logs", READ_WRITE)
            }
            // Net params
            netParamsFile?.also {netParamsFile ->
                addFileSystemBind(netParamsFile.absolutePath, "/opt/corda/network-parameters", READ_WRITE)
            }
            // Node infos
            nodeInfosDir?.also {nodeInfosDir ->
                addFileSystemBind(nodeInfosDir.absolutePath, "/opt/corda/additional-node-infos", READ_WRITE)
            }
        }
    }

    private fun setupNetwork() {
        val rpcPort = simpleNodeConfig.rpcSettings.address!!.port
        val exposedPorts = listOf(rpcPort,
                simpleNodeConfig.rpcSettings.adminAddress!!.port,
                simpleNodeConfig.p2pAddress.port)
        networkAliases.add(nodeLocalFs.nodeHostName)
        addExposedPorts(*exposedPorts.toIntArray())
        this.createContainerCmdModifiers.add(Consumer { cmd ->
            cmd.withHostName(nodeName)
                    .withName(nodeName)
                    .withExposedPorts(exposedPorts.map { port ->
                        ExposedPort.tcp(port)
                    })
        })
    }
}