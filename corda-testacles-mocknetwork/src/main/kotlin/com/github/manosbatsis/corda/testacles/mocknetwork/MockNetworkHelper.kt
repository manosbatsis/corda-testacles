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
package com.github.manosbatsis.corda.testacles.mocknetwork

import com.github.manosbatsis.corda.testacles.mocknetwork.config.MockNetworkConfig
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import net.corda.testing.node.internal.cordappWithPackages


/**
 * Used to create and start/stop a Corda `MockNetwork`.
 *
 * Note that according to Corda's `InternalMockNetwork`,
 * "Using more than one mock network simultaneously is not supported".
 *
 * It is recommended to annotate your test suite
 * classes or methods with `ResourceLock` as shown bellow.
 * That said, you shouldn't need to do that when using `MockNetworkExtension`.
 *
 * ```kotlin
 * @ResourceLock(MockNetworkHelper.RESOURCE_LOCK)
 * ```
 */
@Suppress("MemberVisibilityCanBePrivate")
open class MockNetworkHelper(
        val mockNetworkConfig: MockNetworkConfig
){
    companion object{
        const val RESOURCE_LOCK = "corda-testacles-mocknetwork"
    }

    protected lateinit var mockNetwork: MockNetwork
    protected lateinit var nodesMap: NodeHandles

    /** Obtain the mock network nodes as a [NodeHandles] instance */
    val nodeHandles: NodeHandles
        get() = nodesMap

    /** Start the network */
    fun start() {
        if(!::mockNetwork.isInitialized) {
            mockNetwork = buildMockNetwork()
            nodesMap = buildNodes()
        }
        mockNetwork.startNodes()
        onStarted()
    }

    /** Stop the network */
    fun stop(){
        mockNetwork.stopNodes()
    }

    /**
     * Override to add post-startup initialization logic,
     * e.g. assign individual started nodes to variables for ease of use.
     */
    protected open fun onStarted() {
        // NO-OP
    }

    /** Initialize, but not start, the nodes */
    protected open fun buildNodes(): NodeHandles {
        val mockNodes = mockNetworkConfig.mockNodeParametersList
                .map { it.legalName.toString() to mockNetwork.createNode(it) }
                .toMap()
        return NodeHandles(mockNodes)
    }


    /** Initialize, but not start, the network */
    protected open fun buildMockNetwork(): MockNetwork {
        val scannedPackages = mutableSetOf<String>()
        val cordapps = mutableListOf<TestCordapp>()
        // Add project's cordapp classes, if configured
        mockNetworkConfig.cordappProjectPackage?.also {
            cordapps.add(cordappWithPackages(it))
            scannedPackages.add(it)
        }
        // Add any JAR cordapps based on packages configured
        mockNetworkConfig.cordappPackages
                .map { it.trim() }
                .toSet()
                .filter { it.isNotBlank() }
                .mapTo(cordapps) { TestCordapp.findCordapp(it) }
        return MockNetwork(MockNetworkParameters(
                cordappsForAllNodes = cordapps,
                threadPerNode = mockNetworkConfig.threadPerNode,
                networkParameters = mockNetworkConfig.networkParameters))
    }
}