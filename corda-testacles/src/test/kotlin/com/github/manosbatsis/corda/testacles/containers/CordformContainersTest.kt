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
import com.github.manosbatsis.corbeans.test.containers.SimpleNodeConfig
import com.github.manosbatsis.corbeans.test.containers.getUsers
import com.github.manosbatsis.corda.testacles.Testacle
import com.github.manosbatsis.corda.testacles.TestacleContainers
import com.github.manosbatsis.corda.testacles.containers.boot.Application
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformContainers
import com.github.manosbatsis.corda.testacles.jupiter.TestacleContainersExtension
import net.corda.nodeapi.internal.config.UnknownConfigKeysPolicy
import net.corda.nodeapi.internal.config.parseAs
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
import java.io.File
import java.util.function.Supplier

/**
 *
 */
@TestacleContainers
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Note we are using CorbeansSpringExtension Instead of SpringExtension
@ExtendWith(TestacleContainersExtension::class, SpringExtension::class)
class CordformContainersTest {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CordformContainersTest::class.java)

        @Testacle
        @JvmStatic
        val cordform = CordformContainers(
                File(System.getProperty("user.dir"))
                        .parentFile.parentFile.resolve("partiture/partiture-example-workflow/build/nodes"))

        @DynamicPropertySource
        @JvmStatic
        fun nodeProperties(registry: DynamicPropertyRegistry) {
/*
            registry.add("corbeans.nodes") {
                val nodes: Map<String, NodeParams> = cordform.instances
                        //.filter { (nodeName, instanceAndConf) ->
                        //    instanceAndConf.second.hasPath("notary") == null
                        //}
                        .mapNotNull { (nodeName, instanceAndConf) ->
                            val (container, config) = instanceAndConf
                            val users = if (config.hasPath("rpcUsers")) {
                                // TODO: remove this once config format is updated
                                config.getConfigList("rpcUsers")
                            } else {
                                config.getConfigList("security.authService.dataSource.users")
                            }
                            val nodeConf = config.parseAs<NodeConfig>(UnknownConfigKeysPolicy.IGNORE::handle)

                            nodeName to NodeParams(
                                    partyName = nodeConf.myLegalName.toString(),
                                    username = users.first().getString("user"),
                                    password = users.first().getString("password"),
                                    address = "${container.host}:${container.getMappedPort(nodeConf.rpcSettings.address!!.port)}",
                                    adminAddress = "${container.host}:${container.getMappedPort(nodeConf.rpcSettings.address!!.port)}"
                            )
                        }.toMap()


                logger.info("nodeProperties, nodes: ${nodes}")
                println("nodeProperties, nodes: ${nodes}")
                System.out.println("nodeProperties, nodes: ${nodes}")
                nodes
            }
            */



            cordform.instances
                    .filterNot { (nodeName, instanceAndConf) ->
                        nodeName.toLowerCase().contains("notary")
                                || instanceAndConf.second.hasPath("notary")
                    }
                    .forEach { (nodeName, instanceAndConf) ->
                        val (container, config) = instanceAndConf
                        val users = config.getUsers()
                        val nodeConf = config.parseAs<SimpleNodeConfig>(UnknownConfigKeysPolicy.IGNORE::handle)

                        registry.add("corbeans.nodes.$nodeName.partyName") {
                            "${nodeConf.myLegalName}"
                        }
                        registry.add("corbeans.nodes.$nodeName.username") {
                            users.first().getString("user")
                        }
                        registry.add("corbeans.nodes.$nodeName.password") {
                            users.first().getString("password")
                        }
                        registry.add("corbeans.nodes.$nodeName.address", Supplier {
                            try {
                                val address = "${container.host}:${container.getMappedPort(nodeConf.rpcSettings.address!!.port)}"
                                logger.info("nodeProperties, address: ${address}")
                                println("nodeProperties, address: ${address}")
                                System.out.println("nodeProperties, address: ${address}")
                            }catch (e: Exception){
                                e.printStackTrace()
                                logger.warn("nodeProperties, failed to get addresss", e)
                            }
                            "${container.host}:${container.getMappedPort(nodeConf.rpcSettings.address!!.port)}"
                        })
                        registry.add("corbeans.nodes.$nodeName.adminAddress") {
                            "${container.host}:${container.getMappedPort(nodeConf.rpcSettings.address!!.port)}"
                        }
                        registry.add("corbeans.nodes.$nodeName.admin-address") {
                            "${container.host}:${container.getMappedPort(nodeConf.rpcSettings.address!!.port)}"
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
