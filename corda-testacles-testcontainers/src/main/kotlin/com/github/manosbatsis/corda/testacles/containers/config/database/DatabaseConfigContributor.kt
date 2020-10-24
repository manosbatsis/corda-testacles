package com.github.manosbatsis.corda.testacles.containers.config.database

import com.github.manosbatsis.corda.testacles.containers.config.data.ConfigObjectData
import com.github.manosbatsis.corda.testacles.containers.config.data.ConfigObjectDataContributor

open class DatabaseConfigContributor(
        dataSourceProperties: DatabaseConnectionProperties,
        databaseProperties: DatabaseProperties
): ConfigObjectDataContributor {
    override val dataEntries: List<ConfigObjectData> = listOf(dataSourceProperties)

}