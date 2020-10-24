package com.github.manosbatsis.corda.testacles.containers.config.database

import com.github.manosbatsis.corbeans.test.containers.ConfigUtil
import com.github.manosbatsis.corda.testacles.containers.config.data.ConfigObjectData
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValue
import net.corda.nodeapi.internal.persistence.TransactionIsolationLevel

data class DatabaseProperties (
        var transactionIsolationLevel: TransactionIsolationLevel,
        var runMigration: Boolean
): ConfigObjectData {
    override fun getLocalKey(): String = "database"
    override fun asConfigValue(target: Config): ConfigValue {

        var config: Config = ConfigFactory.empty()
                .withValue("transactionIsolationLevel", ConfigUtil.valueFor(transactionIsolationLevel))
                .withValue("runMigration", ConfigUtil.valueFor(runMigration))
        return config.root()
    }
}