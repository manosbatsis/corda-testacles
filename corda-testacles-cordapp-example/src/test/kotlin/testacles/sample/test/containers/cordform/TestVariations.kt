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