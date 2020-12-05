package com.github.manosbatsis.corda.testacles.common.util

import net.corda.core.serialization.internal._rpcClientSerializationEnv

object SerializationEnvUtil {
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
}