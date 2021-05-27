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
import net.corda.testing.node.internal.cordappWithPackages
import net.corda.testing.node.internal.findCordapp
import org.slf4j.LoggerFactory

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
                // Ignore deprecated API, use cordappsForAllNodes instead
                //.withExtraCordappPackagesToScan(nodeDriverNodesConfig.cordappPackages)
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

    /** Apply any custom config if one was given for it's package */
    protected open fun TestCordapp.maybeWithConfig(packageName: String) =
            nodeDriverNodesConfig.buildCordappConfig(packageName)
                    ?.let { this.withConfig(it) }
                    ?: this

    open fun cordappsForAllNodes(): List<TestCordapp> {
        val scannedPackages = mutableSetOf<String>()
        val cordapps = mutableListOf<TestCordapp>()
        // Add project's cordapp classes, if configured
        nodeDriverNodesConfig.cordappProjectPackage?.also {
            // Apply Cordapp Config if any
            cordapps.add(cordappWithPackages(it).maybeWithConfig(it))
        }
        // Add any JAR cordapps based on packages configured
        nodeDriverNodesConfig.cordappPackages
                .filter { it.isNotBlank() }
                .mapTo(cordapps) { cordappPackage ->
                    logger.debug("Adding cordapp to all driver nodes: {}", cordappPackage)
                    val cordapp = findCordapp(cordappPackage)

                    logger.debug("Found cordapp: $cordapp")
                    // Notify for duplicate scanPackage
                    if (scannedPackages.contains(cordapp.scanPackage))
                        throw IllegalStateException("Duplicate cordapp scanPackage: ${cordapp.scanPackage}")

                    scannedPackages.add(cordapp.scanPackage)
                    // Apply Cordapp Config if any and return
                    cordapp.maybeWithConfig(cordapp.scanPackage)
                }
        return cordapps

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
    open fun getFlowOverrides(): Map<Class<out FlowLogic<*>>, Class<out FlowLogic<*>>> {
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