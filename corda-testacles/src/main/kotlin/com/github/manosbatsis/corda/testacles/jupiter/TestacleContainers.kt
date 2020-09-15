package com.github.manosbatsis.corda.testacles.jupiter

import org.junit.jupiter.api.extension.ExtendWith
import java.lang.annotation.ElementType.TYPE
import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy.RUNTIME
import java.lang.annotation.Target

@Target(TYPE)
@Retention(RUNTIME)
@ExtendWith(TestacleContainersExtension::class)
@Inherited
annotation class Testcontainers(
        /**
         * Whether tests should be disabled (rather than failing)
         * when Docker is not available.
         */
        val disabledWithoutDocker: Boolean = false
)