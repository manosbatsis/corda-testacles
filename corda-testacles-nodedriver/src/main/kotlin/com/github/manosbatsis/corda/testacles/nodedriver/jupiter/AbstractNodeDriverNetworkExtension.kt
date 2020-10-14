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
package com.github.manosbatsis.corda.testacles.nodedriver.jupiter

import com.github.manosbatsis.corda.testacles.nodedriver.NodeDriverHelper
import com.github.manosbatsis.corda.testacles.nodedriver.config.NodeDriverConfig
import com.github.manosbatsis.corda.testacles.nodedriver.config.NodeDriverNodesConfigProvider
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace

/**
 * Base class for extensions that wish to provide a Corda network
 * throughout test suite execution
 */
abstract class AbstractNodeDriverNetworkExtension:
        BeforeAllCallback, AfterAllCallback {

    protected lateinit var nodeDriverHelper: NodeDriverHelper
    protected var started = false

    abstract fun getNamespace(): Namespace

    /** Override to configure the node driver  */
    abstract fun nodeDriverConfigProvider(
            extensionContext: ExtensionContext
    ): NodeDriverNodesConfigProvider

    abstract fun getNodeDriverStoreKey(): String

    /** Start the Corda NodeDriver network */
    override fun beforeAll(extensionContext: ExtensionContext) {
        nodeDriverHelper = NodeDriverHelper(
                NodeDriverConfig(nodeDriverConfigProvider(extensionContext)))
        nodeDriverHelper.start()
        started = true
    }


    /** Stop the Corda network */
    override fun afterAll(extensionContext: ExtensionContext) {
        nodeDriverHelper.stop()
    }
}