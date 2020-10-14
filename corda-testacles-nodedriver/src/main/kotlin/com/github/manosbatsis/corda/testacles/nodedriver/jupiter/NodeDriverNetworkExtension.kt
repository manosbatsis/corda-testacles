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

import com.github.manosbatsis.corda.testacles.model.api.jupiter.JupiterExtensionSupport
import com.github.manosbatsis.corda.testacles.nodedriver.NodeDriverHelper
import com.github.manosbatsis.corda.testacles.nodedriver.NodeHandles
import com.github.manosbatsis.corda.testacles.nodedriver.config.NodeDriverNodesConfig
import org.junit.jupiter.api.extension.ExtensionConfigurationException
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.slf4j.LoggerFactory

/**
 * Base class for extensions that wish to provide a Corda network
 * throughout test suite execution
 */
class NodeDriverNetworkExtension:
        AbstractNodeDriverNetworkExtension(),
        ParameterResolver,
        JupiterExtensionSupport {

    companion object {
        private val logger = LoggerFactory.getLogger(NodeDriverNetworkExtension::class.java)
        private val namespace: Namespace = Namespace.create(NodeDriverNetworkExtension.javaClass)
    }

    override fun getNamespace(): Namespace = namespace
    override fun getNodeDriverStoreKey(): String = NodeDriverHelper::class.java.canonicalName
    override fun nodeDriverConfigProvider(
            extensionContext: ExtensionContext
    ) = findNodeDriverConfig(getRequiredTestClass(extensionContext))

    private fun findNodeDriverConfig(testClass: Class<*>): NodeDriverNodesConfig =
            findNAnnotatedFieldValue(testClass, NodeDriverExtensionConfig::class.java,
                    NodeDriverNodesConfig::class.java)
                    ?:  throw ExtensionConfigurationException(
                            "Could not resolve a NodeDriverNodesConfig. " +
                                    "Either annotate a static field with NodeDriverExtensionConfig " +
                                    "or override nodeDriverConfigProvider(ExtensionContext)")

    override fun supportsParameter(parameterContext: ParameterContext?,
                                   extensionContext: ExtensionContext?) =
            parameterContext?.parameter?.type == NodeHandles::class.java

    override fun resolveParameter(
            parameterContext: ParameterContext?,
            extensionContext: ExtensionContext?
    ) = if(started) nodeDriverHelper.nodeHandles else null

}