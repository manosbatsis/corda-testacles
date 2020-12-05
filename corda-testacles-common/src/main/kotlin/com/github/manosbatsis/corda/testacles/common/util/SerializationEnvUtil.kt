package com.github.manosbatsis.corda.testacles.common.util

import net.corda.core.serialization.internal._allEnabledSerializationEnvs
import net.corda.core.serialization.internal._rpcClientSerializationEnv
import org.slf4j.LoggerFactory

object SerializationEnvUtil {

    @JvmStatic
    private val logger = LoggerFactory.getLogger(SerializationEnvUtil::class.java)

    /**
     * Cleanup RPC client SerializationEnv
     * TODO: this belongs to poolboy, possibly
     */
    fun cleanRpcClientSerializationEnv(){
        // Cleanup RPC client SerializationEnv
        // TODO: this belongs to poolboy, possibly
        _rpcClientSerializationEnv.get()
                ?.also {
                    _rpcClientSerializationEnv.set(null)
                }
    }

    fun listEnabledSerializationEnvs(context: String = ""){
        println("listEnabledSerializationEnvs $context: ")
        _allEnabledSerializationEnvs.forEach {
            println("${it.first}: ${it.second}")
        }
    }
}