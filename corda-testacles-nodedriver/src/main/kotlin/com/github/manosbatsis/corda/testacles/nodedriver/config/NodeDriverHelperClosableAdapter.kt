/*
 * Corda Testacles: Test suite toolkit for Corda developers.
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

import com.github.manosbatsis.corda.testacles.nodedriver.NodeDriverHelper
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource
import org.slf4j.LoggerFactory

class NodeDriverHelperClosableAdapter(
        val nodeDriverHelper: NodeDriverHelper
) : CloseableResource {

    companion object {
        private val logger = LoggerFactory.getLogger(NodeDriverHelperClosableAdapter::class.java)
    }

    init {
        logger.debug("Starting Corda network")
        // Start the network
        this.nodeDriverHelper.start()
    }

    override fun close() {
        logger.debug("Stopping Corda network...")
        this.nodeDriverHelper.stop()
    }

}