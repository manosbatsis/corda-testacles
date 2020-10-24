package com.github.manosbatsis.corda.testacles.containers.config.data

import com.github.manosbatsis.corda.testacles.containers.config.data.ApplyActionType.REPLACE
import com.typesafe.config.Config
import com.typesafe.config.ConfigValue

interface ConfigObjectData {
    /**
     * Get the local path fragment
     * this config data corresponds to
     */
    fun getLocalKey(): String = this.javaClass.simpleName.decapitalize()

    /**
     * Look at the target config and suggest
     * what the action for this fragment should be
     */
    fun getActionHint(target: Config): ApplyActionType = REPLACE

    /**
     * Transform to a [ConfigValue] for the target key,
     * possibly using any information as needed from the target config
     */
    fun asConfigValue(target: Config): ConfigValue
}