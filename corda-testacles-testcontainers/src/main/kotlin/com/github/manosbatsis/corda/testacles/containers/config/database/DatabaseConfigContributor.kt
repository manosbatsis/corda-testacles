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
import com.github.manosbatsis.corda.testacles.containers.config.data.ConfigObjectDataContributor

open class DatabaseConfigContributor(
        dataSourceProperties: DatabaseConnectionProperties,
        databaseProperties: DatabaseProperties?
): ConfigObjectDataContributor {
    override val dataEntries: List<ConfigObjectData> = listOfNotNull(
            dataSourceProperties,
            databaseProperties)

}