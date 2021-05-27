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
package com.github.manosbatsis.corda.testacles.mocknetwork.config

import com.github.manosbatsis.corda.testacles.mocknetwork.util.Capitals
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.NetworkParameters
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.MockNodeParameters

open class MockNetworkConfig(
        val mockNodeParametersList: List<MockNodeParameters>,
        val cordappPackages: List<String>,
        val cordappProjectPackage: String? = null,
        val cordappPackageConfigs: Map<String, Map<String, Any>> = emptyMap(),
        val threadPerNode: Boolean = true,
        val networkParameters: NetworkParameters =
                testNetworkParameters(minimumPlatformVersion = 1),
        val clearEnv: Boolean = false
){

    /** Alternative constructor that builds nodes based on [CordaX500Names]. */
    constructor(
            names: CordaX500Names,
            cordappPackages: List<String>,
            cordappProjectPackage: String? = null,
            cordappPackageConfigs: Map<String, Map<String, Any>> = emptyMap(),
            threadPerNode: Boolean = true,
            networkParameters: NetworkParameters =
                    testNetworkParameters(minimumPlatformVersion = 1),
            clearEnv: Boolean = false
    ): this(names.map {MockNodeParameters(legalName = it)},
            cordappPackages, cordappProjectPackage, cordappPackageConfigs, threadPerNode, networkParameters, clearEnv)


    /**
     * Alternative constructor that builds nodes based on [OrgNames],
     * i.e. one or more strings each one being an organization or X500 name.
     *
     * If not an X500 name, a random locality/country will be selected.
     */
    constructor(
            names: OrgNames,
            cordappPackages: List<String>,
            cordappProjectPackage: String? = null,
            cordappPackageConfigs: Map<String, Map<String, Any>> = emptyMap(),
            threadPerNode: Boolean = true,
            networkParameters: NetworkParameters =
                    testNetworkParameters(minimumPlatformVersion = 1),
            clearEnv: Boolean = false
    ): this(
            CordaX500Names(names.map {
                if (it.contains('=') && it.contains(',')) CordaX500Name.parse(it)
                else with(Capitals.randomCapital()){
                    CordaX500Name(it, name, countryCode)
                }
            }),
            cordappPackages, cordappProjectPackage, cordappPackageConfigs, threadPerNode, networkParameters, clearEnv)

    /**
     * Alternative constructor using the number of nodes needed.
     *
     * The identity (i.e. [CordaX500Name]) for each will be given
     * an organization name as Party1..PartyN along with
     * a random locality/country.
     */
    constructor(
            numberOfNodes: Int,
            cordappPackages: List<String>,
            cordappProjectPackage: String? = null,
            cordappPackageConfigs: Map<String, Map<String, Any>> = emptyMap(),
            threadPerNode: Boolean = true,
            networkParameters: NetworkParameters =
                    testNetworkParameters(minimumPlatformVersion = 1),
            clearEnv: Boolean = false
    ): this(OrgNames((1..numberOfNodes).map { "Party${it}" }),
            cordappPackages, cordappProjectPackage, cordappPackageConfigs, threadPerNode, networkParameters, clearEnv)

}