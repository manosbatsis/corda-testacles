package com.github.manosbatsis.corda.testacles.containers.config

import com.github.manosbatsis.corda.testacles.containers.config.data.ConfigObjectDataContributor
import com.github.manosbatsis.corda.testacles.containers.config.database.DatabaseConnectionProperties
import com.github.manosbatsis.corda.testacles.containers.config.database.JdbcDatabaseContainerDataSourceProperties
import com.github.manosbatsis.corda.testacles.containers.config.drivers.JarsDir
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import org.slf4j.LoggerFactory
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName
import java.io.File

data class NodeContainerConfig(
        val nodeDir: File,
        val imageName: DockerImageName,
        val imageCordaArgs: String,
        val network: Network,
        val nodeHostName: String,
        val nodeConfFile: File = nodeDir.resolve(NodeContainer.NODE_CONF_FILENAME_DEFAULT),
        val netParamsFile: File? = null,
        val nodeInfosDir: File? = null,
        val configContributors: List<ConfigContributor> = emptyList()
) {

    companion object{
        private val logger = LoggerFactory.getLogger(NodeContainerConfig::class.java)
    }

    val driversDir = JarsDir(File(nodeDir, "drivers"))

    val config: Config by lazy { buildConfig() }

    var databaseContainer: JdbcDatabaseContainer<*>? = null

    init{
        configContributors.forEach { contributor ->
            // TODO: aggregate/reuse at upper level, pull here as needed
            applyAnyDatabase(contributor)
        }
    }

    private fun buildConfig(): Config{
        var newConfig = ConfigFactory.parseFile(nodeConfFile)
        configContributors.forEach { contributor ->
            newConfig = contributor.applyConfig(newConfig)
        }

        val configString = newConfig.root().render(
                ConfigRenderOptions.concise().setFormatted(true).setJson(false))
        nodeConfFile.writeText(configString)

        logger.debug("Initialized for node {}, config: {}",
                nodeHostName, configString)
        println("Initialized for node ${nodeHostName}, config: ${configString}")
        return  newConfig
    }

    private fun applyAnyDatabase(contributor: ConfigContributor) {
        if (contributor is ConfigObjectDataContributor) {
            contributor.dataEntries
                    .filterIsInstance(DatabaseConnectionProperties::class.java)
                    .findLast { it.dataSourceClassName.isNotBlank() }
                    ?.also {
                        driversDir.resolveClassJarFilename(it.dataSourceClassName)
                        databaseContainer = if(it is JdbcDatabaseContainerDataSourceProperties<*>)
                            it.container
                        else null

                    }
        }
    }

}