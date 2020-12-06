/*
 * Corda Testacles: Simple conveniences for your Corda Test Suites;
 * because who doesn't need to grow some more of those.
 *
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
package testacles.sample.test.containers.cordform

import com.github.manosbatsis.corda.testacles.containers.config.database.CordformDatabaseSettings
import com.github.manosbatsis.corda.testacles.containers.config.database.CordformDatabaseSettingsFactory
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNetworkContainer
import org.testcontainers.containers.Network
import java.io.File

object Util {
    @JvmStatic
    fun createCordformNetworkContainer(
            dockerImageName: String,
            network: Network = Network.newNetwork(),
            databaseSettings: CordformDatabaseSettings =
                    CordformDatabaseSettingsFactory.H2
    ): CordformNetworkContainer {
        return CordformNetworkContainer(
                imageName = dockerImageName,
                network = network,
                nodesDir = File(System.getProperty("user.dir"))
                        .parentFile.resolve("build/nodes"),
                cloneNodesDir = true,
                databaseSettings = databaseSettings,
                privilegedMode = false,
                clearEnv = true)
    }
}