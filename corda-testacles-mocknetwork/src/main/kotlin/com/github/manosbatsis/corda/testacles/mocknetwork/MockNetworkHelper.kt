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
import com.github.manosbatsis.corda.testacles.mocknetwork.util.Capitals
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.NetworkParameters
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.internal.chooseIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp


/** Used to create and start/stop a Corda `MockNetwork` */
@Suppress("MemberVisibilityCanBePrivate")
open class MockNetworkHelper(
        val mockNodeParametersList: List<MockNodeParameters>
) {
    /** Alternative constructor using a list of [CordaX500Name]s */
    constructor(vararg names: CordaX500Name) : this(names.map {
        MockNodeParameters(legalName = it)
    })

    /**
     * Alternative constructor using a list of organization or X500 names.
     * If not an X500 name, a random locality/country will be selected.
     */
    constructor(vararg names: String) : this(
            *names.map {
                if (it.contains('=') && it.contains(',')) CordaX500Name.parse(it)
                else with(Capitals.randomCapital()){
                    CordaX500Name(it, name, countryCode)
                }
            }.toTypedArray()
    )

    /**
     * Alternative constructor using only the number of nodes needed.
     * The identity (i.e. [CordaX500Name]) for each will be given
     * an organization name as Party1..PartyN along with
     * a random locality/country.
     */
    constructor(numberOfNodes: Int) : this(
            *(1..numberOfNodes).map { "Party${it}" }.toTypedArray()
    )

    /** Alternative constructor using a [MockNetworkConfig] */
    constructor(
            mockNetworkConfig: MockNetworkConfig
    ): this(*mockNetworkConfig.cordappPackages.toTypedArray()) {
        threadPerNode = mockNetworkConfig.threadPerNode
        networkParameters = mockNetworkConfig.networkParameters
        cordappPackages.addAll(mockNetworkConfig.cordappPackages)
    }

    protected lateinit var mockNetwork: MockNetwork
    protected lateinit var nodesMap: Map<Any, StartedMockNode>
    protected var cordappPackages = mutableListOf<String>()
    protected var threadPerNode = true
    protected var networkParameters = testNetworkParameters(minimumPlatformVersion = 1)

    /** Obtain the mock network nodes as a [NodeHandles] instance */
    val nodeHandles: NodeHandles
        get() = NodeHandles(nodesMap)

    /** Adds cordapp packages to be resolved as [TestCordapp]s */
    fun withCordappPackages(cordappPackages: Iterable<String>): MockNetworkHelper {
        this.cordappPackages.addAll(cordappPackages)
        return this
    }

    /** Sets whether to use a thread per node, optional */
    fun withThreadPerNode(threadPerNode: Boolean): MockNetworkHelper {
        this.threadPerNode = threadPerNode
        return this
    }

    /** Sets the [NetworkParameters] to use, optional */
    fun withNetworkParameters(networkParameters: NetworkParameters): MockNetworkHelper {
        this.networkParameters = networkParameters
        return this
    }

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
    protected open fun buildNodes() =
        mockNodeParametersList
                .map {
                    val node = mockNetwork.createNode(it)
                    val identity = node.info.chooseIdentity()
                    mapOf(
                            identity to node,
                            identity.name to node,
                            identity.name.toString() to node,
                            identity.name.organisation to node,
                            identity.name.organisation.replace(" ", "").decapitalize() to node
                    )
                }
                .flatMap { it.entries }
                .map { it.key to it.value }.toMap()


    /** Initialize, but not start, the network */
    protected open fun buildMockNetwork(): MockNetwork =
            MockNetwork(MockNetworkParameters(
                    cordappsForAllNodes = cordappPackages
                            .map { it.trim() }
                            .toSet()
                            .filter { it.isNotBlank() }
                            .map { TestCordapp.findCordapp(it) },
                    threadPerNode = threadPerNode,
                    networkParameters = networkParameters))
}