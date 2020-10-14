/*
 * Corda Testacles: Test suite toolkit for Corda developers.
 * Copyright (C) 2020 Manos Batsis
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
package com.github.manosbatsis.corda.testacles.jupiter

import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName
import java.io.File

/**
 * Used to annotate a [File] field of a test suite
 * as the one to be used by [CordformNetworkExtension]
 * for Corda node containers as the_nodes_ directory,
 * i.e. one created by the `Cordform` Gradle plugin.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class NodesDir

/**
 * Used to annotate a [Network] field of a test suite
 * as the one to be used by [CordformNetworkExtension]
 * for Corda node containers
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class NodesNetwork

/**
 * Used to annotate a [DockerImageName] field of a test suite
 * as the one to be used by [CordformNetworkExtension]
 * for Corda node containers
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class NodesImageName