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
package com.github.manosbatsis.corda.testacles.containers.cordform

import com.github.manosbatsis.corda.testacles.containers.base.KPostgreSQLContainer
import com.github.manosbatsis.corda.testacles.containers.config.database.DatabaseConnectionProperties
import com.github.manosbatsis.corda.testacles.containers.config.database.DatabaseProperties
import com.github.manosbatsis.corda.testacles.containers.config.database.H2DataSourceProperties
import com.github.manosbatsis.corda.testacles.containers.config.database.PostgreSQLContainerDataSourceProperties
import net.corda.nodeapi.internal.persistence.TransactionIsolationLevel
import net.corda.nodeapi.internal.persistence.TransactionIsolationLevel.READ_COMMITTED
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.Network

data class DatabaseSettings(
        val databaseConnectionProperties: DatabaseConnectionProperties,
        val jdbcDatabaseContainer: JdbcDatabaseContainer<*>?,
        val databaseProperties: DatabaseProperties = DatabaseProperties(READ_COMMITTED, false)
)

interface CordformDatabaseSettings{
    val databaseProperties: DatabaseProperties
    fun buildDatabaseSettings(
            nodeName: String,
            network: Network
    ): DatabaseSettings
    fun withTransactionIsolationLevel(
            transactionIsolationLevel: TransactionIsolationLevel
    ): CordformDatabaseSettings {
        this.databaseProperties.transactionIsolationLevel = transactionIsolationLevel
        return this
    }

    fun withRunMigration(
            runMigration: Boolean
    ): CordformDatabaseSettings {
        this.databaseProperties.runMigration = runMigration
        return this
    }
}

enum class CordformDatabaseSettingsFactory(
        override val databaseProperties: DatabaseProperties =
                DatabaseProperties(READ_COMMITTED, false)
): CordformDatabaseSettings {
    H2{
        override fun buildDatabaseSettings(
                nodeName: String,
                network: Network
        ) = DatabaseSettings(H2DataSourceProperties, null)
    },
    POSTGRES{
        override fun buildDatabaseSettings(
                nodeName: String,
                network: Network
        ): DatabaseSettings {
            val container = KPostgreSQLContainer()
                    .withNetwork(network)
                    .withNetworkAliases("${nodeName}Db")
            return DatabaseSettings(
                    PostgreSQLContainerDataSourceProperties(container), container)

        }
    }

}