/*
 * Corda Testacles: Test suite toolkit for Corda developers.
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
package com.github.manosbatsis.corda.testacles.model.api

import com.github.manosbatsis.corda.testacles.model.api.CordaTestaclesDtoStrategyKeys.DEFAULT
import com.github.manosbatsis.corda.testacles.model.api.CordaTestaclesDtoStrategyKeys.LITE
import kotlin.reflect.KClass


enum class CordaTestaclesDtoStrategyKeys(val classNameSuffix: String) {
    DEFAULT("Dto"),
    LITE("LiteDto");


    override fun toString(): String {
        return this.classNameSuffix
    }

    companion object {
        fun findFromString(s: String): CordaTestaclesDtoStrategyKeys? {
            val sUpper = s.toUpperCase()
            return CordaTestaclesDtoStrategyKeys.values()
                    .find {
                        it.name.toUpperCase() == sUpper
                                || it.classNameSuffix.toUpperCase() == sUpper
                    }
        }

        fun getFromString(s: String): CordaTestaclesDtoStrategyKeys = findFromString(s)
                ?: error("Could not match input $s to CordaTestaclesDtoStrategyKeys entry")

    }

}

/**
 * Marks the [primarySource] as a code generation source
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class CordaTestaclesModelMixin(
        val ignoreProperties: Array<String> = [],
        val primarySource: KClass<*>,
        val copyAnnotationPackages: Array<String> = [],
        val strategies: Array<CordaTestaclesDtoStrategyKeys> = [DEFAULT, LITE]
)