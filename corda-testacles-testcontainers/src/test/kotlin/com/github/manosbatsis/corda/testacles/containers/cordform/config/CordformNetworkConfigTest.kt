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