/*
 * Corda Testacles: Test suite toolkit for Corda developers.
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

import com.github.dockerjava.api.model.ExposedPort
import com.github.manosbatsis.corbeans.test.containers.ConfigUtil.getUsers
import com.github.manosbatsis.corda.testacles.containers.cordform.fs.NodeLocalFs
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer
import com.github.manosbatsis.corda.testacles.model.SimpleNodeConfig
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.nodeapi.internal.config.UnknownConfigKeysPolicy.IGNORE
import net.corda.nodeapi.internal.config.User
import net.corda.nodeapi.internal.config.parseAs
import org.slf4j.LoggerFactory
import org.testcontainers.containers.BindMode.READ_WRITE
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.util.function.Consumer

class CordformNodeContainer(
        dockerImageName: DockerImageName = DockerImageName.parse("corda/corda-zulu-java1.8-4.5"),
        val nodeLocalFs: NodeLocalFs
) : GenericContainer<CordformNodeContainer>(dockerImageName), NodeContainer {

    companion object {
        private val logger = LoggerFactory.getLogger(CordformNodeContainer::class.java)
    }

    val config: Config = ConfigFactory.parseFile(nodeLocalFs.nodeConfFile)

    override val nodeName: String = nodeLocalFs.nodeHostName

    override val simpleNodeConfig: SimpleNodeConfig = config.parseAs(IGNORE::handle)

    override val nodeIdentity: CordaX500Name = simpleNodeConfig.myLegalName

    override val rpcNetworkHostAndPort by lazy {
        NetworkHostAndPort(host, getMappedPort(simpleNodeConfig.rpcSettings.address!!.port))
    }

    override val rpcAddress: String by lazy { rpcNetworkHostAndPort.toString() }

    override val rpcUsers: List<User> = getUsers(config)

    private val rpcConnections: MutableMap<User, CordaRPCOps> = mutableMapOf()

    // Initialize network alias, ports, FS binds
    init {
        logger.debug("Initializing from: ${nodeLocalFs.nodeDir?.absolutePath}")
        setupNetwork()
        setupFileSystemBinds()
    }

    override fun getRpc(user: User) = rpcConnections.getOrPut(user) {
        NodeContainer.createRpcConnection(this, user).proxy
    }

    private fun setupFileSystemBinds() = with(nodeLocalFs) {
        // Setup node.conf
        addFileSystemBind(nodeConfFile.absolutePath, "/etc/corda/node.conf", READ_WRITE)
        // Setup Corda dir
        nodeDir?.also { nodeDir ->
            addFileSystemBind(nodeDir.absolutePath, "/etc/corda", READ_WRITE)
            addFileSystemBind(nodeDir.resolve("cordapps").absolutePath,
                    "/opt/corda/cordapps", READ_WRITE)
            addFileSystemBind(nodeDir.resolve("certificates").absolutePath,
                    "/opt/corda/certificates", READ_WRITE)
            addFileSystemBind(nodeDir.absolutePath, "/opt/corda/persistence", READ_WRITE)
            addFileSystemBind(nodeDir.resolve("logs").absolutePath,
                    "/opt/corda/logs", READ_WRITE)
        }
        // Setup net params
        netParamsFile?.also { netParamsFile ->
            addFileSystemBind(netParamsFile.absolutePath, "/opt/corda/network-parameters", READ_WRITE)
        }
        // Setup node infos dir
        nodeInfosDir?.also { nodeInfosDir ->
            addFileSystemBind(nodeInfosDir.absolutePath, "/opt/corda/additional-node-infos", READ_WRITE)
        }
    }


    private fun setupNetwork() {
        // Setup network alias
        networkAliases.add(nodeLocalFs.nodeHostName)
        // Setup ports
        val rpcPort = simpleNodeConfig.rpcSettings.address!!.port
        val exposedPorts = listOf(rpcPort,
                simpleNodeConfig.rpcSettings.adminAddress!!.port,
                simpleNodeConfig.p2pAddress.port)
        addExposedPorts(*exposedPorts.toIntArray())
        this.createContainerCmdModifiers.add(Consumer() { cmd ->
            cmd.withHostName(nodeName)
                    .withExposedPorts(exposedPorts.map { port ->
                        ExposedPort.tcp(port)
                    })
        })
    }
}