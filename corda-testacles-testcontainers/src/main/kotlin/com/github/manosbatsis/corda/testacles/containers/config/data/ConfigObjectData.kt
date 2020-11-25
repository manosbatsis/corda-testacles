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

import com.github.manosbatsis.corda.testacles.containers.config.data.ApplyActionType.REPLACE
import com.typesafe.config.Config
import com.typesafe.config.ConfigValue

interface ConfigObjectData {
    /**
     * Get the local path fragment
     * this config data corresponds to
     */
    fun getLocalKey(): String = this.javaClass.simpleName.decapitalize()

    /**
     * Look at the target config and suggest
     * what the action for this fragment should be
     */
    fun getActionHint(target: Config): ApplyActionType = REPLACE

    /**
     * Transform to a [ConfigValue] for the target key,
     * possibly using any information as needed from the target config
     */
    fun asConfigValue(target: Config): ConfigValue
}