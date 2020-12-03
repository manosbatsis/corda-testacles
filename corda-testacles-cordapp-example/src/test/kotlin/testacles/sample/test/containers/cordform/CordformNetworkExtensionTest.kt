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

import com.github.manosbatsis.corda.testacles.containers.config.database.CordformDatabaseSettingsFactory.POSTGRES
import com.github.manosbatsis.corda.testacles.containers.cordform.config.CordaNetworkConfig
import com.github.manosbatsis.corda.testacles.containers.cordform.config.CordformNetworkConfig
import com.github.manosbatsis.corda.testacles.jupiter.CordaNetwork
import com.github.manosbatsis.corda.testacles.jupiter.CordformNetworkExtension
import com.github.manosbatsis.corda.testacles.jupiter.NodesDir
import com.github.manosbatsis.corda.testacles.jupiter.NodesImageName
import com.github.manosbatsis.corda.testacles.jupiter.NodesNetwork
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Network
import testacles.sample.test.containers.cordform.TestVariations.Companion.cordaVersionOs
import testacles.sample.test.containers.cordform.base.CordformNetworkExtensionTestBase
import java.io.File

/** Sample test using [CordformNetworkExtension] */
@ExtendWith(CordformNetworkExtension::class)
@Tags(Tag("cordform"))
class CordformNetworkExtensionTest : CordformNetworkExtensionTestBase() {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CordformNetworkExtensionTest::class.java)

        // Note: Ignored if a [CordaNetworkConfig]-annotated
        // field is present.
        @NodesImageName
        @JvmStatic
        val nodesImageName = cordaVersionOs()

        // Optional, defaults to new network
        // Note: Ignored if a [CordaNetworkConfig]-annotated
        // field is present.
        @NodesNetwork
        @JvmStatic
        val nodesNetwork: Network = Network.newNetwork()

        // Optional, defaults to auto-lookup (build/nodes, ../build/nodes)
        // Note: Ignored if a [CordaNetworkConfig]-annotated
        // field is present.
        @NodesDir
        @JvmStatic
        val nodesDir = File(System.getProperty("user.dir"))
                .parentFile.resolve("build/nodes")

        // Optional, provides the Corda network config to the extension.
        // When using this all other extension config annotations
        // willbe ignored (@NodesImageName, @NodesNetwork and @NodesDir)
        @CordaNetwork
        @JvmStatic
        val networkConfig: CordaNetworkConfig = CordformNetworkConfig(
                nodesDir = nodesDir,
                imageName = nodesImageName,
                network = nodesNetwork,
                // Create a Postgres DB for each node (default is H2)
                databaseSettings = POSTGRES)

    }
}
