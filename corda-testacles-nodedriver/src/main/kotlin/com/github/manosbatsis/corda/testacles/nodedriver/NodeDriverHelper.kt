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
package com.github.manosbatsis.corda.testacles.nodedriver

import com.github.manosbatsis.corda.rpc.poolboy.config.NodeParams
import com.github.manosbatsis.corda.testacles.nodedriver.config.NodeDriverConfig
import com.github.manosbatsis.corda.testacles.nodedriver.config.NodeDriverNodesConfig
import net.corda.core.concurrent.CordaFuture
import net.corda.core.identity.CordaX500Name
import net.corda.nodeapi.internal.ShutdownHook
import net.corda.nodeapi.internal.addShutdownHook
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.NodeHandle
import net.corda.testing.driver.NodeParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.User
import net.corda.testing.node.internal.DriverDSLImpl
import net.corda.testing.node.internal.TestCordappInternal
import net.corda.testing.node.internal.setDriverSerialization
import net.corda.testing.node.internal.waitForShutdown
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor


/**
 * Uses Corda's node driver to either:
 *
 * - Explicitly start/shutdown a Corda network
 * - Run some code within the context of an implicit ad hoc Corda network via [withDriverNodes]
 *
 * This helper class is not threadsafe as concurrent networks would result in port conflicts.
 */
open class NodeDriverHelper(
        private val nodeDriverConfig: NodeDriverConfig
)  {

    companion object {
        private val logger = LoggerFactory.getLogger(NodeDriverHelper::class.java)
        @Suppress("DEPRECATION")
        fun createDriver(defaultParameters: DriverParameters ): DriverDSLImpl {
            val driverParams = mapOf(
                    "portAllocation" to defaultParameters.portAllocation,
                    "debugPortAllocation" to defaultParameters.debugPortAllocation,
                    "systemProperties" to defaultParameters.systemProperties,
                    "driverDirectory" to defaultParameters.driverDirectory.toAbsolutePath(),
                    "useTestClock" to defaultParameters.useTestClock,
                    "isDebug" to defaultParameters.isDebug,
                    "startNodesInProcess" to defaultParameters.startNodesInProcess,
                    "waitForAllNodesToFinish" to defaultParameters.waitForAllNodesToFinish,
                    "extraCordappPackagesToScan" to emptyList<String>(),//@Suppress("DEPRECATION") defaultParameters.extraCordappPackagesToScan,
                    "notarySpecs" to defaultParameters.notarySpecs,
                    "jmxPolicy" to defaultParameters.jmxPolicy,
                    "compatibilityZone" to null,
                    "networkParameters" to defaultParameters.networkParameters,
                    "notaryCustomOverrides" to defaultParameters.notaryCustomOverrides,
                    "inMemoryDB" to defaultParameters.inMemoryDB,
                    "cordappsForAllNodes" to defaultParameters.cordappsForAllNodes as Collection<TestCordappInternal>?,
                    "djvmBootstrapSource" to defaultParameters.djvmBootstrapSource,
                    "djvmCordaSource" to defaultParameters.djvmCordaSource,
                    "environmentVariables" to defaultParameters.environmentVariables,
                    "enableSNI" to true,
                    "allowHibernateToManageAppSchema" to true,
                    "premigrateH2Database" to true

            )
            return createDriver(DriverDSLImpl::class, driverParams)
        }


        /**
         * A reflection-based tmp fix for managing differences
         * between Corda 4.0 and 4.6, also servig as a workaround for
         * - https://github.com/corda/corda/issues/6826
         * - https://r3-cev.atlassian.net/browse/CORDA-4090
         */
        fun <T: Any> createDriver(
                targetClass: KClass<T>,
                parameters: Map<String,Any?>,
                targetConstructor: KFunction<T> = targetClass.primaryConstructor!!
        ):T{
            val constructorParameters = targetConstructor.parameters.associateBy({it},{ parameters[it.name] })
            return targetConstructor.callBy(constructorParameters)
        }
    }

    constructor(
            nodeDriverNodesConfig: NodeDriverNodesConfig
    ): this(NodeDriverConfig(nodeDriverNodesConfig))

    private var shutdownHook: ShutdownHook? = null
    private var driverNodes: NodeHandles? = null
    private lateinit var driverDsl: DriverDSLImpl


    val nodeHandles: NodeHandles
        get() = driverNodes ?: error("nodeHandles have not been initialized")


    fun start() {
        try {
            driverDsl = createDriver(nodeDriverConfig.driverParameters())
            setDriverSerialization(driverDsl.cordappsClassLoader)
            shutdownHook = addShutdownHook(driverDsl::shutdown)
            driverDsl.start()
            driverNodes = NodeHandles(startNodes())
        } catch (e: Exception) {
            logger.error("Driver shutting down because of exception", e)
            stop()
            throw e
        }
    }

    fun stop() {
        try {
            driverNodes?.nodesByName?.values?.forEach{
                it.waitForShutdown()
                it.stop()
            }
            driverDsl.shutdown()
        } catch (e: Exception) {
            logger.error("Error during driver shut down", e)
        }
        shutdownHook?.cancel()
        setDriverSerialization(null)
    }


    /**
     * Launch a network, execute the action code, and shut the network down
     */
    fun withDriverNodes(action: () -> Unit) {

        // start the driver, using with* to avoid CE 4.2 error
        driver(nodeDriverConfig.driverParameters()) {
            // Configure nodes per application.properties
            val nodeHandles = NodeHandles(startNodes())
            action() // execute code in context
        }
    }

    private fun startNodes(): Map<String, NodeHandle> {
        return startNodeFutures().mapValues {
            val handle: NodeHandle = it.value.get()
            logger.debug("startNodes started node ${it.key}")
            handle
        }
    }

    private fun startNodeFutures(): Map<String, CordaFuture<NodeHandle>> {
        // Note addresses to filter out any dupes
        val startedRpcAddresses = mutableSetOf<String>()
        logger.debug("startNodeFutures: starting node, cordaNodesProperties: {}",
                nodeDriverConfig.nodeDriverNodesConfig)
        return nodeDriverConfig.getNodeParams().mapNotNull {
            val nodeName = it.key
            val nodeParams = it.value
            val testPartyName = nodeParams.partyName
            val x500Name = if (testPartyName != null) CordaX500Name.parse(testPartyName)
            else CordaX500Name(nodeName, "Athens", "GR")

            // Only start a node per unique address,
            // ignoring "default" overrides
            if (!startedRpcAddresses.contains(nodeParams.address)
                    && nodeName != NodeParams.NODENAME_DEFAULT) {

                logger.debug("startNodeFutures: starting node, params: {}", nodeParams)
                // note the address as started
                startedRpcAddresses.add(nodeParams.address!!)

                val user = User(nodeParams.username!!, nodeParams.password!!, setOf("ALL"))
                @Suppress("UNUSED_VARIABLE")
                val nodeParameters = NodeParameters()
                        .withFlowOverrides(nodeDriverConfig.getFlowOverrides())
                        .withRpcUsers(listOf(user))
                nodeName to driverDsl.startNode(
                        defaultParameters = nodeParameters,
                        providedName = x500Name,
                        //rpcUsers = listOf(user),
                        customOverrides = mapOf(
                                "rpcSettings.address" to nodeParams.address,
                                "rpcSettings.adminAddress" to nodeParams.adminAddress))
            } else {
                logger.debug("startNodeFutures: skipping node: {}", nodeParams)
                null
            }
        }.toMap()
    }


}