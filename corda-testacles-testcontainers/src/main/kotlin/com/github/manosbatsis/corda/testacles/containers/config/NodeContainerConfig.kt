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
import java.io.File

data class NodeContainerConfig(
        val nodeDir: File,
        override val imageName: String,
        val imageCordaArgs: String,
        val network: Network,
        val nodeHostName: String,
        val nodeConfFile: File = nodeDir.resolve(NodeContainer.NODE_CONF_FILENAME_DEFAULT),
        val netParamsFile: File? = null,
        val nodeInfosDir: File? = null,
        val configContributors: List<ConfigContributor> = emptyList(),
        override val entryPointOverride: List<String> = emptyList()
): NodeImageNameConfig {

    companion object{
        private val logger = LoggerFactory.getLogger(NodeContainerConfig::class.java)
    }


    val driversDir = JarsDir(File(nodeDir, "drivers"))

    val config: Config by lazy { buildConfig() }

    var databaseContainer: JdbcDatabaseContainer<*>? = null

    init{
        configContributors.forEach { contributor ->
            // TODO: aggregate/reuse at upper level, pull here as needed
            initAnyDatabaseContainer(contributor)
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
        return  newConfig
    }

    private fun initAnyDatabaseContainer(contributor: ConfigContributor) {
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