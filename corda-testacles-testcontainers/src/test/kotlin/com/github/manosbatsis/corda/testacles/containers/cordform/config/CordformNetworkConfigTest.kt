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
package com.github.manosbatsis.corda.testacles.containers.cordform.config

import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig.Companion.CORDA_CE_ALPINE_ZULU_4_5_2
import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig.Companion.CORDA_CE_ALPINE_ZULU_4_6
import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig.Companion.CORDA_CE_ALPINE_ZULU_4_6_1
import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig.Companion.CORDA_OS_ZULU_4_5
import com.github.manosbatsis.corda.testacles.containers.config.NodeImageNameConfig.Companion.CORDA_OS_ZULU_4_6
import com.github.manosbatsis.corda.testacles.containers.cordform.config.CordformNetworkConfig.Companion.ENTRYPOINT_WITH_MIGRATIONS_FIRST_4_6
import com.github.manosbatsis.corda.testacles.containers.cordform.config.CordformNetworkConfig.Companion.buildEntryPointOverride
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CordformNetworkConfigTest {

    @Test
    fun buildEntryPointOverrideTests(){
        // 4.5
        assertTrue(buildEntryPointOverride(CORDA_OS_ZULU_4_5).isEmpty())
        assertTrue(buildEntryPointOverride(CORDA_CE_ALPINE_ZULU_4_5_2).isEmpty())
        // 4.6
        assertEquals(ENTRYPOINT_WITH_MIGRATIONS_FIRST_4_6,
                buildEntryPointOverride(CORDA_OS_ZULU_4_6).firstOrNull())
        assertEquals(ENTRYPOINT_WITH_MIGRATIONS_FIRST_4_6,
                buildEntryPointOverride(CORDA_CE_ALPINE_ZULU_4_6).firstOrNull())
        assertEquals(ENTRYPOINT_WITH_MIGRATIONS_FIRST_4_6,
                buildEntryPointOverride(CORDA_CE_ALPINE_ZULU_4_6_1).firstOrNull())

         

    }
}