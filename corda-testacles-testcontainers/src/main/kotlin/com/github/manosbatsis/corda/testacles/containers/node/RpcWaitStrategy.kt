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
package com.github.manosbatsis.corda.testacles.containers.node

import com.github.manosbatsis.corda.rpc.poolboy.connection.NodeRpcConnection
import com.github.manosbatsis.corda.testacles.common.corda.SimpleNodeConfig
import com.github.manosbatsis.corda.testacles.containers.config.NodeContainerConfig
import com.github.manosbatsis.corda.testacles.containers.util.ConfigUtil
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.nodeapi.internal.config.UnknownConfigKeysPolicy.IGNORE
import net.corda.nodeapi.internal.config.User
import net.corda.nodeapi.internal.config.parseAs
import org.rnorth.ducttape.TimeoutException
import org.rnorth.ducttape.unreliables.Unreliables
import org.slf4j.LoggerFactory
import org.testcontainers.containers.ContainerLaunchException
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy
import java.util.concurrent.TimeUnit.SECONDS


open class RpcWaitStrategy(
        val nodeContainerConfig: NodeContainerConfig
) : AbstractWaitStrategy() {

    companion object {
        private val logger = LoggerFactory.getLogger(RpcWaitStrategy::class.java)
    }

    override fun waitUntilReady() {
        val containerName = waitStrategyTarget.containerInfo.name
        try {
            Unreliables.retryUntilSuccess(startupTimeout.seconds.toInt(), SECONDS) {
                var success = false
                rateLimiter.doWhenReady{
                    success = attemptRpcRequest()
                }
                success
            }
        } catch (e: TimeoutException) {
            throw ContainerLaunchException(
                    "Timed out waiting for container RPC to be accessible ($containerName)")
        }
    }

    open fun attemptRpcRequest(): Boolean =
            try {
                buildRpcConnection().proxy.partiesFromName("dummy", true)
                Thread.sleep(2000)
                true
            }
            catch (e: Throwable){
                false
            }



    /** Build the [NodeRpcConnection] used to check if the container is ready. */
    fun buildRpcConnection(): NodeRpcConnection {
        val simpleNodeConfig: SimpleNodeConfig = nodeContainerConfig.config.parseAs(IGNORE::handle)
        val nodeIdentity: CordaX500Name = simpleNodeConfig.myLegalName
        val rpcAddress = NetworkHostAndPort(
                host = waitStrategyTarget.host,
                port = waitStrategyTarget.getMappedPort(simpleNodeConfig.rpcSettings.address!!.port)
        )
        val rpcUser: User = ConfigUtil.getUsers(nodeContainerConfig.config).first()
        return NodeContainer.createRpcConnection(
                nodeIdentity = nodeIdentity,
                rpcAddress = "$rpcAddress",
                eager = false,
                disableGracefulReconnect = true,
                user = rpcUser
        )
    }


}
