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
package com.github.manosbatsis.corda.testacles.containers.config.database

import com.github.manosbatsis.corda.testacles.containers.base.KPostgreSQLContainer
import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig.Companion.BASE_VERSION_4_6
import com.github.manosbatsis.corda.testacles.containers.cordform.config.CordaNetworkConfig
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

data class DatabaseSettings(
        val databaseConnectionProperties: DatabaseConnectionProperties,
        val jdbcDatabaseContainer: JdbcDatabaseContainer<*>?,
        val databaseProperties: DatabaseProperties? = null
)

interface CordformDatabaseSettings{
    val databaseProperties: DatabaseProperties?
    fun buildDatabaseSettings(
            nodeName: String,
            networkConfig: CordaNetworkConfig
    ): DatabaseSettings
}

enum class CordformDatabaseSettingsFactory(
        override val databaseProperties: DatabaseProperties? = null
): CordformDatabaseSettings {
    H2{
        override fun buildDatabaseSettings(
                nodeName: String,
                networkConfig: CordaNetworkConfig
        ) = DatabaseSettings(
                databaseConnectionProperties = H2DataSourceProperties,
                jdbcDatabaseContainer = null,
                databaseProperties = buildDatabaseProperties(networkConfig))
    },
    POSTGRES{
        override fun buildDatabaseSettings(
                nodeName: String,
                networkConfig: CordaNetworkConfig
        ): DatabaseSettings {
            val container = KPostgreSQLContainer(
                    DockerImageName.parse("${PostgreSQLContainer.IMAGE}:9.6.20"))
                    .withNetwork(networkConfig.network)
                    .withNetworkAliases("${nodeName}Db")
            return DatabaseSettings(
                    databaseConnectionProperties = PostgreSQLContainerDataSourceProperties(container),
                    jdbcDatabaseContainer = container,
                    databaseProperties = buildDatabaseProperties(networkConfig, "READ_COMMITTED"))

        }
    };

    companion object{
        fun buildDatabaseProperties(
                networkConfig: CordaNetworkConfig,
                transactionIsolationLevel: String? = null
        ): DatabaseProperties?{
            return with(networkConfig){
                when {
                    getVersion() >= BASE_VERSION_4_6 -> null
                    isEnterprise() && getVersion() < BASE_VERSION_4_6 ->
                        DatabaseProperties(
                                transactionIsolationLevel = transactionIsolationLevel,
                                runMigration = true,
                                initialiseAppSchema = "UPDATE")

                    else -> DatabaseProperties(transactionIsolationLevel = transactionIsolationLevel)
                }
            }
        }
    }

}