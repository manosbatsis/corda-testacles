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
package com.github.manosbatsis.corda.testacles.containers.config.data

import com.github.manosbatsis.corda.testacles.containers.config.ConfigContributor
import com.github.manosbatsis.corda.testacles.containers.config.data.ApplyActionType.ADD_MISSING
import com.github.manosbatsis.corda.testacles.containers.config.data.ApplyActionType.CLEAR
import com.github.manosbatsis.corda.testacles.containers.config.data.ApplyActionType.REPLACE
import com.typesafe.config.Config

interface ConfigObjectDataContributor: ConfigContributor {
    val dataEntries: List<ConfigObjectData>

    override val paths: Set<String>
        get() = dataEntries.map { it.getLocalKey() }.toSet()

    override fun applyConfig(target: Config, paths: Set<String>): Config {
        var resultConfig = target
        dataEntries.forEach { configData ->
            val key = configData.getLocalKey()
            resultConfig = when(configData.getActionHint(resultConfig)){
                REPLACE -> resultConfig.withValue(key, configData.asConfigValue(target))
                CLEAR -> resultConfig.withoutPath(configData.getLocalKey())
                ADD_MISSING -> {
                    // TODO: current impl only works for top-level entries
                    val path = configData.getLocalKey()
                    if(target.hasPath(path)) resultConfig
                    else resultConfig.withValue(key, configData.asConfigValue(target))

                }}
        }
        return resultConfig
    }
}