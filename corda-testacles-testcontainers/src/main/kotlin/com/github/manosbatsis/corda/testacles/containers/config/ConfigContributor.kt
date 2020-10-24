package com.github.manosbatsis.corda.testacles.containers.config

import com.typesafe.config.Config

interface ConfigContributor {
    val paths: Set<String>
    fun applyConfig(
            target: Config, paths: Set<String> = this.paths
    ): Config
}