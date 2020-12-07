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

import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import com.github.manosbatsis.corda.testacles.containers.config.database.CordformDatabaseSettingsFactory.POSTGRES
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNetworkContainer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.Network
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import testacles.sample.client.Application
import testacles.sample.cordapp.workflow.YoDto
import testacles.sample.test.containers.cordform.TestVariations.Companion.cordaVersionCe
import testacles.sample.test.containers.cordform.Util.createCordformNetworkContainer


/** A RESTful Spring Boot test using [CordformNetworkContainer] */
@Testcontainers
@Suppress(names = ["SpringJavaInjectionPointsAutowiringInspection"])
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@Tags(Tag("cordform"))
// Run a single network at a time
// @ResourceLock(CordformNetworkContainer.RESOURCE_LOCK)
class CordformNetworkContainerSpringBootTest {

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
            cordformNetworkContainer.nodes
                    .filterNot { (nodeName, instance) ->
                        nodeName.toLowerCase().contains("notary")
                                || instance.config.hasPath("notary")
                    }
                    .forEach { (nodeName, container) ->
                        val nodeConf = container.simpleNodeConfig
                        val user = container.getDefaultRpcUser()
                        val prefix = "corbeans.nodes.$nodeName"
                        logger.info("nodeProperties:" +
                                "\n$prefix.partyName=${nodeConf.myLegalName}" +
                                "\n$prefix.username=${user.username}" +
                                "\n$prefix.password=${user.password}" +
                                "\n$prefix.partyName=${nodeConf.myLegalName}" +
                                "\n$prefix.address=${container.rpcAddress}")
                        with(registry) {
                            add("$prefix.partyName") { "${nodeConf.myLegalName}" }
                            add("$prefix.username") { user.username }
                            add("$prefix.password") { user.password }
                            add("$prefix.address") { container.rpcAddress }
                            add("$prefix.adminAddress") { container.rpcAddress }
                            add("$prefix.admin-address") { container.rpcAddress }
                        }
                    }
        }
    }

    // autowire a network service, used to access node services
    @Autowired
    lateinit var networkService: CordaNetworkService

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `Can retrieve node identity`() {
        val entity = this.restTemplate.getForEntity("/api/nodes/partya/whoami", Any::class.java)
        Assertions.assertEquals(OK, entity.statusCode)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
    }

    @Test
    fun `Can send a yo `() {
        logger.debug("Network info: ${this.networkService.getInfo()}")
        val yoDto = YoDto(
                recipient = this.networkService.getNodeService("partyb").nodeLegalName,
                message = "Yo from A to B!")
        val yoState = this.restTemplate.postForEntity(
                "/api/nodes/partya/yo", yoDto, Any::class.java)
        Assertions.assertEquals(CREATED, yoState.statusCode)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.type, yoState.headers.contentType?.type)
        Assertions.assertEquals(MediaType.APPLICATION_JSON.subtype, yoState.headers.contentType?.subtype)
    }

}

