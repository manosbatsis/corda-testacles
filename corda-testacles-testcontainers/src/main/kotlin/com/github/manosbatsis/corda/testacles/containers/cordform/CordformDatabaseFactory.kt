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