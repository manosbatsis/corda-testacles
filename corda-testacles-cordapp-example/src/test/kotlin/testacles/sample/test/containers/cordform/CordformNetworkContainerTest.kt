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
package testacles.sample.test.containers.cordform

import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNetworkContainer
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.parallel.ResourceLock
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Network
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import testacles.sample.test.containers.cordform.TestVariations.Companion.cordaVersionOs
import testacles.sample.test.containers.cordform.base.CordformNetworkContainerTestBase
import testacles.sample.test.containers.cordform.base.Util.createCordformNetworkContainer


/** An RPC-based test using [CordformNetworkContainer] */
@Testcontainers
@Tags(Tag("cordform"))
// Run a single network at a time
@ResourceLock(CordformNetworkContainer.RESOURCE_LOCK)
class CordformNetworkContainerTest : CordformNetworkContainerTestBase(){

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CordformNetworkContainerTest::class.java)

        @Container
        @JvmStatic
        val networkContainer = createCordformNetworkContainer(
                    dockerImageName = cordaVersionOs(),
        network = Network.newNetwork())
    }

    override fun getCordformNetworkContainer(): CordformNetworkContainer {
        return networkContainer
    }

}
