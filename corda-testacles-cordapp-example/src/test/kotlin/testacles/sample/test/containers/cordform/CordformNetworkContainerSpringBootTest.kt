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
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.Network
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import testacles.sample.test.boot.Application
import testacles.sample.test.containers.cordform.TestVariations.Companion.cordaVersionCe
import testacles.sample.test.containers.cordform.base.CordformNetworkContainerSpringBootTestBase
import testacles.sample.test.containers.cordform.base.Util.createCordformNetworkContainer


/** A RESTful Spring Boot test using [CordformNetworkContainer] */
@Testcontainers
@Suppress(names = ["SpringJavaInjectionPointsAutowiringInspection"])
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@Tags(Tag("cordform"))
class CordformNetworkContainerSpringBootTest: CordformNetworkContainerSpringBootTestBase() {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CordformNetworkContainerSpringBootTest::class.java)

        @JvmStatic
        val network = Network.newNetwork()

        @Container
        @JvmStatic
        val cordformNetworkContainer = createCordformNetworkContainer(
                        network = network,
                        dockerImageName = cordaVersionCe(),
                        databaseSettings = POSTGRES)

        @DynamicPropertySource
        @JvmStatic
        fun nodesProperties(registry: DynamicPropertyRegistry) {
            nodesProperties(registry, cordformNetworkContainer)
        }
    }
}

