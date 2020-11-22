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
package com.github.manosbatsis.corda.testacles.nodedriver.config

import com.github.manosbatsis.corda.rpc.poolboy.config.NodeParams
import com.github.manosbatsis.corda.testacles.nodedriver.NodeDriverHelper
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.IllegalFlowLogicException
import net.corda.core.node.NetworkParameters
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.driver.DriverParameters
import net.corda.testing.node.NotarySpec
import net.corda.testing.node.TestCordapp
import net.corda.testing.node.internal.findCordapp
import org.slf4j.LoggerFactory
import java.util.Properties

open class NodeDriverConfig(
        val nodeDriverNodesConfig: NodeDriverNodesConfig
) {

    companion object {
        private val logger = LoggerFactory.getLogger(NodeDriverConfig::class.java)
    }

    open fun driverParameters(): DriverParameters {
        return DriverParameters()
                .withIsDebug(false)
                .withStartNodesInProcess(true)
                .withCordappsForAllNodes(cordappsForAllNodes())
                .withNotarySpecs(notarySpecs())
                .withNotaryCustomOverrides(notaryCustomOverrides())
                .withNetworkParameters(customizeTestNetworkParameters(
                        testNetworkParameters(minimumPlatformVersion =
                        nodeDriverNodesConfig.minimumPlatformVersion)))
    }

    open fun customizeTestNetworkParameters(
            testNetworkParameters: NetworkParameters
    ): NetworkParameters {
        return testNetworkParameters
    }

    open fun cordappsForAllNodes(): List<TestCordapp> {
        val scanPackages = mutableSetOf<String>()
        return nodeDriverNodesConfig.cordapPackages
                .filter { it.isNotBlank() }
                .mapNotNull { cordappPackage ->
                    logger.debug("Adding cordapp to all driver nodes: {}", cordappPackage)
                    val cordapp = findCordapp(cordappPackage)
                    // skip if dupe
                    if (scanPackages.contains(cordapp.scanPackage)) null
                    else {
                        scanPackages.add(cordapp.scanPackage)
                        cordappWithConfig(cordappPackage, cordapp)
                    }
                }
    }

    open fun cordappWithConfig(cordappPackage: String, testCordapp: TestCordapp): TestCordapp {
        val config = buildCordappConfig(cordappPackage)
        return if (config != null) testCordapp.withConfig(config) else testCordapp
    }

    /**
     * Override to provide the Cordapp config for a target package.
     * Defaults in looking for "$cordappPackage.config.properties"
     * in the (test) classpath.
     */
    open fun buildCordappConfig(cordappPackage: String): Map<String, Any>? {
        val configProperties = this.javaClass.classLoader
                .getResourceAsStream("$cordappPackage.config.properties")
        if (configProperties != null) {
            val properties = Properties()
            properties.load(configProperties)
            val config: Map<String, Any> = properties
                    .map { it.key.toString() to it.value }
                    .toMap()
            return config
        }
        return null
    }

    open fun notarySpecs(): List<NotarySpec> {
        val notarySpecs = listOf(NotarySpec(
                name = DUMMY_NOTARY_NAME,
                validating = !nodeDriverNodesConfig.notarySpec.nonValidating))
        return notarySpecs
    }

    open fun notaryCustomOverrides(): Map<String, Any?> =
            if (nodeDriverNodesConfig.notarySpec.address != null)
                mapOf("rpcSettings.address" to nodeDriverNodesConfig.notarySpec.address)
            else emptyMap()


    /**
     * Load node config from spring-boot application
     */
    @Suppress("MemberVisibilityCanBePrivate")
    open fun getNodeParams(): Map<String, NodeParams> {
        logger.debug("getNodeParams called")
        return if (this.nodeDriverNodesConfig.nodes.isNotEmpty()) {
            this.nodeDriverNodesConfig.nodes
        } else {
            throw RuntimeException("Could not find node configurations in application properties")
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    open fun getFlowOverrides(): Map<out Class<out FlowLogic<*>>, out Class<out FlowLogic<*>>> {
        return this.nodeDriverNodesConfig.flowOverrides.flatMap {
            it.replace(',', ' ').split(' ')
                    .filter { it.isNotBlank() }
        }
                .map { validatedFlowClassFromName(it.trim()) }
                .chunked(2)
                .associate { (a, b) -> a to b }
    }

    open fun validatedFlowClassFromName(flowClassName: String): Class<out FlowLogic<*>> {
        logger.debug("validatedFlowClassFromName: '${flowClassName}'")
        val forName = try {
            Class.forName(flowClassName, true, NodeDriverHelper::class.java.classLoader)
        } catch (e: ClassNotFoundException) {
            throw IllegalFlowLogicException(flowClassName, "Flow not found: $flowClassName")
        }
        return forName.asSubclass(FlowLogic::class.java)
                ?: throw IllegalFlowLogicException(flowClassName, "The class $flowClassName is not a subclass of FlowLogic.")
    }
}