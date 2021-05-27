package com.github.manosbatsis.corda.testacles.common.corda

import java.util.*

interface CordappsConfig {

    val cordappPackages: List<String>
    val cordappProjectPackage: String?
    val cordappPackageConfigs: Map<String, Map<String, Any>>

    /**
     * Override to provide the Cordapp config for a target package.
     * Defaults in looking for "$cordappPackage.config.properties"
     * in the (test) classpath, as well as a matching entry in [cordappPackageConfigs].
     */
    open fun buildCordappConfig(cordappPackage: String): Map<String, Any>? {
        val cordappPackageConfig = cordappPackageConfigs[cordappPackage] ?: emptyMap()
        val configProperties = this.javaClass.classLoader
                .getResourceAsStream("$cordappPackage.config.properties")
        val configPropertiesConfig = configProperties?.let {
            val properties = Properties()
            properties.load(configProperties)
            properties.map { it.key.toString() to it.value }.toMap()
        } ?: emptyMap()
        return (cordappPackageConfig + configPropertiesConfig).let {
            if(it.isNotEmpty()) it else null
        }
    }
}