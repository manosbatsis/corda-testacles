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
import com.github.manosbatsis.corda.rpc.poolboy.config.PoolParams
import com.github.manosbatsis.corda.testacles.nodedriver.config.TestNotaryProperties
import com.github.manosbatsis.corda.testacles.nodedriver.config.nodeDriverConfig
import com.github.manosbatsis.partiture.flow.PartitureFlow
import mypackage.cordapp.contract.YoContract
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.driver.internal.incrementalPortAllocation

object TestConfigUtil {

    val portAllocation = incrementalPortAllocation()

    fun myCustomNodeDriverConfig() = nodeDriverConfig {
        cordapPackages = listOf(YoContract::class.java.`package`.name,
                PartitureFlow::class.java.`package`.name)
        nodes = mapOf("partya" to ALICE_NAME.toNodeParams(), "partyb" to BOB_NAME.toNodeParams())
        notarySpec = TestNotaryProperties()
        flowOverrides = emptyList()
        poolParams = PoolParams()
        minimumPlatformVersion = 5
    }

    fun CordaX500Name.toNodeParams() = NodeParams.mergeParams(NodeParams(
            partyName = "${this}",
            username = "user1",
            password = "test",
            address = "${portAllocation.nextHostAndPort()}",
            adminAddress = "${portAllocation.nextHostAndPort()}",
            disableGracefulReconnect = true))
}