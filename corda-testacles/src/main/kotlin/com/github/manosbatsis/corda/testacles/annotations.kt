/*
 * Corda Testacles: Test containers and tools to help cordapps grow.
 * Copyright (C) 2018 Manos Batsis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package com.github.manosbatsis.corda.testacles

import com.github.manosbatsis.corda.testacles.jupiter.TestacleContainersExtension
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Modeled after the [org.testcontainers.junit.jupiter.Testcontainers]
 * annotation.
 */
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(TestacleContainersExtension::class)
@Target(AnnotationTarget.CLASS)
annotation class TestacleContainers(
        /**
         * Whether tests should be disabled
         * (rather than failing) when Docker is not available.
         */
        val disabledWithoutDocker: Boolean = false
)

/**
 * Modeled after the [org.testcontainers.junit.jupiter.Container]
 * annotation.
 */

/**
 * The `@Testacle` annotation is used in conjunction with the [TestacleContainers] annotation
 * to mark containers that should be managed by the Testcontainers extension.
 *
 * @see TestacleContainers
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Testacle()