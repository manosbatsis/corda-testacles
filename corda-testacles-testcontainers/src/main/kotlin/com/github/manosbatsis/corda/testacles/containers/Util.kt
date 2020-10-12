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
package com.github.manosbatsis.corbeans.test.containers

import com.typesafe.config.Config
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueFactory
import net.corda.nodeapi.internal.config.User
import org.testcontainers.containers.GenericContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.utility.DockerImageName


open class KImageNameContainer(
        dockerImageName: DockerImageName
) : GenericContainer<KImageNameContainer>(dockerImageName)

class KGenericContainer(
        dockerImage: ImageFromDockerfile
) : GenericContainer<KGenericContainer>(dockerImage)


object ConfigUtil {
    fun <T> valueFor(any: T): ConfigValue = ConfigValueFactory.fromAnyRef(any)

    fun getUsers(config: Config) =
            if (config.hasPath("rpcUsers")) {
                config.getConfigList("rpcUsers")
            } else {
                config.getConfigList("security.authService.dataSource.users")
            }.map {
                User(username = if (it.hasPath("username")) it.getString("username") else it.getString("user"),
                        password = it.getString("password"),
                        permissions = it.getStringList("permissions").toSet())
            }

}