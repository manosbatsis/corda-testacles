package com.github.manosbatsis.corda.testacles.containers.config.data

import com.github.manosbatsis.corda.testacles.containers.config.ConfigContributor
import com.github.manosbatsis.corda.testacles.containers.config.data.ApplyActionType.ADD_MISSING
import com.github.manosbatsis.corda.testacles.containers.config.data.ApplyActionType.CLEAR
import com.github.manosbatsis.corda.testacles.containers.config.data.ApplyActionType.REPLACE
import com.typesafe.config.Config

interface ConfigObjectDataContributor: ConfigContributor {
    val dataEntries: List<ConfigObjectData>

    override val paths: Set<String>
        get() = dataEntries.map { it.getLocalKey() }.toSet()

    override fun applyConfig(target: Config, paths: Set<String>): Config {
        var resultConfig = target
        dataEntries.forEach { configData ->
            val key = configData.getLocalKey()
            resultConfig = when(configData.getActionHint(resultConfig)){
                REPLACE -> resultConfig.withValue(key, configData.asConfigValue(target))
                CLEAR -> resultConfig.withoutPath(configData.getLocalKey())
                ADD_MISSING -> {
                    // TODO: current impl only works for top-level entries
                    val path = configData.getLocalKey()
                    if(target.hasPath(path)) resultConfig
                    else resultConfig.withValue(key, configData.asConfigValue(target))

                }}
        }
        return resultConfig
    }
}