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
package com.github.manosbatsis.corda.testacles.containers

import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import com.github.manosbatsis.corda.testacles.containers.boot.Application
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNodesContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File

/**
 *
 */
@Testcontainers
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Note we are using CorbeansSpringExtension Instead of SpringExtension
@ExtendWith(SpringExtension::class)
class CordformContainersTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CordformContainersTest::class.java)

        @Container
        @JvmStatic
        val cordform = CordformNodesContainer(
                File(System.getProperty("user.dir"))
                        .parentFile.parentFile.resolve("partiture/partiture-example-workflow/build/nodes"))

        @DynamicPropertySource
        @JvmStatic
        fun nodeProperties(registry: DynamicPropertyRegistry) {
            cordform.instances
                    .filterNot { (nodeName, instance) ->
                        nodeName.toLowerCase().contains("notary")
                                || instance.config.hasPath("notary")
                    }
                    .forEach { (nodeName, container) ->
                        val nodeConf = container.simpleNodeConfig
                        val user = container.getDefaultRpcUser()

                        registry.add("corbeans.nodes.$nodeName.partyName") {
                            "${nodeConf.myLegalName}"
                        }
                        registry.add("corbeans.nodes.$nodeName.username") {
                            user.username
                        }
                        registry.add("corbeans.nodes.$nodeName.password") {
                            user.password
                        }
                        registry.add("corbeans.nodes.$nodeName.address") {
                            logger.info("nodeProperties, address: ${container.rpcAddress}")
                            container.rpcAddress
                        }
                        registry.add("corbeans.nodes.$nodeName.adminAddress") {
                            container.rpcAddress
                        }
                        registry.add("corbeans.nodes.$nodeName.admin-address") {
                            container.rpcAddress
                        }
                    }


        }
    }

    // autowire a network service, used to access node services
    @Autowired
    lateinit var networkService: CordaNetworkService

    @Autowired
    lateinit var restTemplateOrig: TestRestTemplate

    val restTemplate: TestRestTemplate by lazy {
        //restTemplateOrig.restTemplate.requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
        //restTemplateOrig.restTemplate.interceptors.add(RequestResponseLoggingInterceptor())
        restTemplateOrig
    }

    @Test
    fun `Can retrieve node identity`() {
        val service = this.networkService.getNodeService("partyA")
        assertNotNull(service.nodeIdentity)
        val entity = this.restTemplate.getForEntity("/api/nodes/partyA/whoami", Any::class.java)
        assertEquals(HttpStatus.OK, entity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
    }


}
