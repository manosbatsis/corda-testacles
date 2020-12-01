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

import com.github.manosbatsis.corda.testacles.containers.config.data.ApplyActionType
import com.github.manosbatsis.corda.testacles.containers.config.data.ApplyActionType.CLEAR
import com.github.manosbatsis.corda.testacles.containers.config.data.ConfigObjectData
import com.github.manosbatsis.corda.testacles.containers.util.ConfigUtil
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValue
import net.corda.core.identity.CordaX500Name
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer
import java.net.InetAddress

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
    override val dataSource: DataSource by lazy {
        // Modify the JDBC URL to use our alias
        var networkJdbcUrl = container.getJdbcUrl()
        val aliases = container.getNetworkAliases()

        // We're looking for an alis with "Db" suffix
        val dbContainerAlias: String = aliases.find { it.endsWith("Db") }
                ?: error("Could not find valid alias of database container")
        // ... to replace any of the folowing
        val localhost = InetAddress.getLocalHost()
        mutableSetOf<String>(
                "localhost", "127.0.0.1", "docker",
                localhost.canonicalHostName,
                localhost.hostName,
                localhost.hostAddress)
                .forEach{
                    networkJdbcUrl = networkJdbcUrl.replace(it, dbContainerAlias)
                }

        // Build and return the datasource properties
        DataSource(
                url = networkJdbcUrl.replace(
                        container.getFirstMappedPort().toString(),
                        container.getExposedPorts().first().toString()),
                user = container.getUsername(),
                password = container.getPassword())
        }

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