package com.github.manosbatsis.corda.testacles.containers.config

import com.github.manosbatsis.corda.testacles.containers.util.Version

interface NodeImageNameConfig {

    companion object{
        private const val ENTERPRISE = "enterprise"
        const val CORDA_OS_ZULU_4_4 = "corda/corda-zulu-java1.8-4.4"
        const val CORDA_OS_ZULU_4_5 = "corda/corda-zulu-java1.8-4.5"
        const val CORDA_OS_ZULU_4_6 = "corda/corda-zulu-java1.8-4.6"
        const val CORDA_CE_ALPINE_ZULU_4_4_4 = "corda/corda-enterprise-node-alpine-zulu-java1.8-4.4.4"
        const val CORDA_CE_ALPINE_ZULU_4_5_2 = "corda/corda-enterprise-node-alpine-zulu-java1.8-4.5.2"
        const val CORDA_CE_ALPINE_ZULU_4_6 = "corda/corda-enterprise-node-alpine-zulu-java1.8-4.6"
        const val CORDA_CE_ALPINE_ZULU_4_6_1 = "corda/corda-enterprise-node-alpine-zulu-java1.8-4.6.1"

        val BASE_VERSION_4_0 = Version("4.0")
        val BASE_VERSION_4_1 = Version("4.1")
        val BASE_VERSION_4_2 = Version("4.2")
        val BASE_VERSION_4_3 = Version("4.3")
        val BASE_VERSION_4_4 = Version("4.4")
        val BASE_VERSION_4_5 = Version("4.5")
        val BASE_VERSION_4_6 = Version("4.6")

        fun isEnterprise(imageName: String) = imageName.toLowerCase().contains(ENTERPRISE)

        fun buildVersion(imageName: String): Version {
            return imageName.split("-").toMutableList().run {
                if (last().toLowerCase() == "snapshot") removeAt(size - 1)
                Version(this.last())
            }
        }
    }

    /** The name of the docker image used */
    val imageName: String
    /**  Custom entrypoint to use if non-empty. */
    val entryPointOverride: List<String>
    /** The version of the docker image used */
    fun getVersion(): Version = buildVersion(imageName)
    /** Whether the docker image used is based on Corda Enterprise */
    fun isEnterprise(): Boolean = isEnterprise(imageName)
}