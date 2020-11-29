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

import com.github.manosbatsis.corda.testacles.containers.config.data.ConfigObjectData
import com.github.manosbatsis.corda.testacles.containers.util.ConfigUtil
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValue

/** Represents a node.conf database section, ignored for Corda 4.6+ */
data class DatabaseProperties (
        var transactionIsolationLevel: String? = null,
        var runMigration: Boolean? = null,
        var initialiseSchema: Boolean? = runMigration,
        val initialiseAppSchema: String? = null
): ConfigObjectData {
    override fun getLocalKey(): String = "database"
    override fun asConfigValue(target: Config): ConfigValue {

        var config: Config = ConfigFactory.empty()

        // Add non-null paths
        transactionIsolationLevel?.also{
            config = config.withValue("transactionIsolationLevel",
                    ConfigUtil.valueFor(transactionIsolationLevel))
        }
        runMigration?.also{
            config = config.withValue("runMigration",
                    ConfigUtil.valueFor(runMigration))
        }
        initialiseSchema?.also{
            config = config.withValue("initialiseSchema",
                    ConfigUtil.valueFor(initialiseSchema))
        }
        initialiseAppSchema?.also{
            config = config.withValue("initialiseAppSchema",
                    ConfigUtil.valueFor(initialiseAppSchema))
        }
        return config.root()
    }
}