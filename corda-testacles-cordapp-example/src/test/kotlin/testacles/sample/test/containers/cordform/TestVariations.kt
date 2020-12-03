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
package testacles.sample.test.containers.cordform

import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig
import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig.Companion.BASE_VERSION_4_4
import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig.Companion.BASE_VERSION_4_5
import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig.Companion.BASE_VERSION_4_6

class TestVariations {
    companion object{

        /** Set by gradle test tasks */
        private const val CORDA_VARIATION_VERSION = "CORDA_VARIATION_VERSION"
        private val cordaVersion
                get() = System.getProperty(CORDA_VARIATION_VERSION)
                        ?: System.getenv(CORDA_VARIATION_VERSION) ?: "4.6"

        private val osVersions = mapOf(
                "$BASE_VERSION_4_4" to NodeImageNameConfig.CORDA_OS_ZULU_4_4,
                "$BASE_VERSION_4_5" to NodeImageNameConfig.CORDA_OS_ZULU_4_5,
                "$BASE_VERSION_4_6" to NodeImageNameConfig.CORDA_OS_ZULU_4_6)
        private val ceVersions = mapOf(
                "$BASE_VERSION_4_4" to NodeImageNameConfig.CORDA_CE_ALPINE_ZULU_4_4_4,
                "$BASE_VERSION_4_5" to NodeImageNameConfig.CORDA_CE_ALPINE_ZULU_4_5_2,
                "$BASE_VERSION_4_6" to NodeImageNameConfig.CORDA_CE_ALPINE_ZULU_4_6_1)

        fun cordaVersionOs() = osVersions[cordaVersion]
                ?: error("Cannot find a matching Corda OS variation for version $cordaVersion")
        fun cordaVersionCe() = ceVersions[cordaVersion]
                ?: error("Cannot find a matching Corda Enterprise variation for version $cordaVersion")
        init {
            println("Tests using corda version: $cordaVersion")
        }
    }
}