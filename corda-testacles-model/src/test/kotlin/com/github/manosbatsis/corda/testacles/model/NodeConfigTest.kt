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
package com.github.manosbatsis.corda.testacles.model

import com.github.manosbatsis.corbeans.test.containers.simpleNodeConf
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.node.services.config.NodeRpcSettings
import net.corda.nodeapi.internal.config.User
import org.junit.jupiter.api.Test

class NodeConfigTest {

    @Test
    fun `Can build simple node config`(){
        simpleNodeConf{
            //baseDirectory = File(System.getProperty("user.dir"), "build/node1").toPath()
            devMode = true
            myLegalName = CordaX500Name.parse("O=PartyA,L=London,C=GB")
            p2pAddress = NetworkHostAndPort("localhost", 10200)
            rpcSettings = NodeRpcSettings(
                    address = NetworkHostAndPort("0.0.0.0", 10201),
                    adminAddress = NetworkHostAndPort("0.0.0.0", 10241),
                    useSsl = false,
                    ssl = null
            )
            rpcUsers {
                User(
                        username = "user1",
                        password = "test",
                        permissions = setOf("ALL")
                )
            }
        }
    }
}