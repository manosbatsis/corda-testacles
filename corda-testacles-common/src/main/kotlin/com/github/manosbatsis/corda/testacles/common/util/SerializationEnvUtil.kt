package com.github.manosbatsis.corda.testacles.common.util

import net.corda.core.serialization.internal._allEnabledSerializationEnvs
import net.corda.core.serialization.internal._contextSerializationEnv
import net.corda.core.serialization.internal._driverSerializationEnv
import net.corda.core.serialization.internal._inheritableContextSerializationEnv
import net.corda.core.serialization.internal._rpcClientSerializationEnv
import net.corda.coretesting.internal.inVMExecutors
import org.slf4j.LoggerFactory

object SerializationEnvUtil {

    @JvmStatic
    private val logger = LoggerFactory.getLogger(SerializationEnvUtil::class.java)
    private val serializationEnvFields = listOf(
            _driverSerializationEnv,
            _contextSerializationEnv,
            _inheritableContextSerializationEnv,
            _rpcClientSerializationEnv
    )
    /**
     * Cleanup RPC client SerializationEnv
     * TODO: this belongs to poolboy, possibly
     */
    fun cleanRpcClientSerializationEnv(){
        _rpcClientSerializationEnv.get()
                ?.also {
                    _rpcClientSerializationEnv.set(null)
                }
    }

    fun clearEnv(){
        serializationEnvFields.forEach { toggleField ->
            if(toggleField.get() != null) toggleField.set(null)
        }
        inVMExecutors.clear()
    }

    fun listEnabledSerializationEnvs(context: String = ""){
        println("listEnabledSerializationEnvs $context: ")
        _allEnabledSerializationEnvs.forEach {
            println("${it.first}: ${it.second}")
        }
    }
}