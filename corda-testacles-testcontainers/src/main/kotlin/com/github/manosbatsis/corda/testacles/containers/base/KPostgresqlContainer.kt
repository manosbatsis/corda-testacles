package com.github.manosbatsis.corda.testacles.containers.base

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName


class KPostgreSQLContainer(
        fullImageName: DockerImageName =
                DockerImageName.parse("$IMAGE:$DEFAULT_TAG")
) : PostgreSQLContainer<KPostgreSQLContainer>(fullImageName)