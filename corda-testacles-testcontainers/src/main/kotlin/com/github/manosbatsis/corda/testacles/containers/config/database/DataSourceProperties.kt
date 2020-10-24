package com.github.manosbatsis.corda.testacles.containers.config.database

import com.github.manosbatsis.corbeans.test.containers.ConfigUtil
import com.github.manosbatsis.corda.testacles.containers.config.data.ApplyActionType
import com.github.manosbatsis.corda.testacles.containers.config.data.ApplyActionType.CLEAR
import com.github.manosbatsis.corda.testacles.containers.config.data.ConfigObjectData
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValue
import net.corda.core.identity.CordaX500Name
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer

interface DatabaseConnectionProperties: ConfigObjectData{
    val dataSource: DataSource
    val dataSourceClassName: String

    override fun getLocalKey(): String = "dataSourceProperties"

    override fun asConfigValue(target: Config): ConfigValue {
        var config: Config = ConfigFactory.empty()
                .withValue("dataSource", dataSource.asConfigValue(target))
                .withValue("dataSourceClassName", ConfigUtil.valueFor(dataSourceClassName))
        return config.root()
    }
}

object H2DataSourceProperties: DatabaseConnectionProperties{
    override val dataSource = DataSource("none", null, null)
    override val dataSourceClassName: String = ""
    override fun getActionHint(target: Config): ApplyActionType = CLEAR
}

abstract class JdbcDatabaseContainerDataSourceProperties<C: JdbcDatabaseContainer<*>>(
        val container: C
): DatabaseConnectionProperties{
    override val dataSource: DataSource
        get() = DataSource(
                url = container.getJdbcUrl()
                        .replace("localhost", container.getNetworkAliases().last())
                        .replace("127.0.0.1", container.getNetworkAliases().last())
                        .replace(container.getFirstMappedPort().toString(),
                                container.getExposedPorts().first().toString()),
                user = container.getUsername(),
                password = container.getPassword()
        )

}

class PostgreSQLContainerDataSourceProperties(
        container: PostgreSQLContainer<*>
): JdbcDatabaseContainerDataSourceProperties<PostgreSQLContainer<*>>(container){
    override val dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
}

data class DataSourceProperties(
        override val dataSource: DataSource,
        override val dataSourceClassName: String
): DatabaseConnectionProperties

data class DataSource(
        val url: String,
        val user: String?,
        val password: String?,
        val isBaseUrl: Boolean = false
): ConfigObjectData {
    override fun asConfigValue(target: Config): ConfigValue {
        val jdbcUrl = if(isBaseUrl) {
            val nodeName = CordaX500Name.parse(target.getString("myLegalName"))
                    .organisation.replace(" ", "_").toLowerCase()
            "${url}/${nodeName}"
        }else url
        var config: Config = ConfigFactory.empty()
                .withValue("url", ConfigUtil.valueFor(jdbcUrl))
        if(user != null) config = config.withValue("user", ConfigUtil.valueFor(user))
        if(password != null) config = config.withValue("password", ConfigUtil.valueFor(password))
        return config.root()
    }
}