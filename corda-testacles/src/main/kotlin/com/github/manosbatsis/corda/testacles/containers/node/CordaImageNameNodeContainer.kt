package com.github.manosbatsis.corda.testacles.containers.node

import com.github.dockerjava.api.model.ExposedPort
import com.github.manosbatsis.corbeans.test.containers.ConfigUtil
import com.github.manosbatsis.corda.testacles.containers.cordform.fs.NodeLocalFs
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion.createRpcConnection
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

class CordaImageNameNodeContainer(
        dockerImageName: DockerImageName = DockerImageName.parse("corda/corda-zulu-java1.8-4.5"),
        val nodeLocalFs: NodeLocalFs
): GenericContainer<CordaImageNameNodeContainer>(dockerImageName), NodeContainer {

    companion object{
        private val logger = LoggerFactory.getLogger(CordaImageNameNodeContainer::class.java)
    }

    val config: Config = ConfigFactory.parseFile(nodeLocalFs.nodeConfFile)

    override val nodeName: String = nodeLocalFs.nodeHostName

    override val simpleNodeConfig: SimpleNodeConfig = config.parseAs(IGNORE::handle)

    override val nodeIdentity: CordaX500Name = simpleNodeConfig.myLegalName

    override val rpcNetworkHostAndPort by lazy {
        NetworkHostAndPort(host, getMappedPort(simpleNodeConfig.rpcSettings.address!!.port))
    }

    override val rpcAddress: String by lazy { rpcNetworkHostAndPort.toString() }

    override val rpcUsers: List<User> = ConfigUtil.getUsers(config)

    private val rpcConnections: MutableMap<User, CordaRPCOps> = mutableMapOf()

    // Initialize network alias, ports, FS binds
    init {
        logger.debug("Initializing from: ${nodeLocalFs.nodeDir?.absolutePath}")
        setupNetwork()
        setupFileSystemBinds()
    }

    override fun getRpc(user: User) = rpcConnections.getOrPut(user) {
        createRpcConnection(this, user).proxy
    }

    private fun setupFileSystemBinds() = with(nodeLocalFs) {
        // Setup node.conf
        addFileSystemBind(nodeConfFile.absolutePath, "/etc/corda/node.conf", READ_WRITE)
        // Setup Corda dir
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
        // Setup net params
        netParamsFile?.also {netParamsFile ->
            addFileSystemBind(netParamsFile.absolutePath, "/opt/corda/network-parameters", READ_WRITE)
        }
        // Setup node infos dir
        nodeInfosDir?.also {nodeInfosDir ->
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
        this.createContainerCmdModifiers.add(Consumer { cmd ->
            cmd.withHostName(nodeName)
                    .withName(nodeName)
                    .withExposedPorts(exposedPorts.map { port ->
                        ExposedPort.tcp(port)
                    })
        })
    }
}