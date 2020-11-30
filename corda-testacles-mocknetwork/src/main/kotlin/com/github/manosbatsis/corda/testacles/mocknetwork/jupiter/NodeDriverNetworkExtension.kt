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
package com.github.manosbatsis.corda.testacles.mocknetwork.jupiter

import com.github.manosbatsis.corda.testacles.mocknetwork.MockNetworkHelper
import com.github.manosbatsis.corda.testacles.mocknetwork.NodeHandles
import com.github.manosbatsis.corda.testacles.mocknetwork.config.MockNetworkConfig
import com.github.manosbatsis.corda.testacles.model.api.jupiter.JupiterExtensionSupport
import org.junit.jupiter.api.extension.ExtensionConfigurationException
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.slf4j.LoggerFactory

/**
 * Base class for extensions that wish to provide a Corda [MockNetwork]
 * throughout test suite execution
 */
class MockNetworkExtension:
        AbstractMockNetworkExtension(),
        ParameterResolver,
        JupiterExtensionSupport {

    companion object {
        private val logger = LoggerFactory.getLogger(MockNetworkExtension::class.java)
        private val namespace: Namespace = Namespace.create(MockNetworkExtension::class.java)
    }

    override fun getNamespace(): Namespace = namespace
    override fun getMockNetworkStoreKey(): String = MockNetworkHelper::class.java.canonicalName
    override fun getMockNetworkConfig(
            extensionContext: ExtensionContext
    ) = findMockNetworkConfig(getRequiredTestClass(extensionContext))

    private fun findMockNetworkConfig(testClass: Class<*>): MockNetworkConfig =
            findNAnnotatedFieldValue(testClass, MockNetworkExtensionConfig::class.java,
                    MockNetworkConfig::class.java)
                    ?:  throw ExtensionConfigurationException(
                            "Could not resolve a MockNetworkConfig. " +
                                    "Either annotate a static field with MockNetworkExtensionConfig " +
                                    "or override findMockNetworkConfig(ExtensionContext)")

    override fun supportsParameter(parameterContext: ParameterContext?,
                                   extensionContext: ExtensionContext?) =
            parameterContext?.parameter?.type == NodeHandles::class.java

    override fun resolveParameter(
            parameterContext: ParameterContext?,
            extensionContext: ExtensionContext?
    ) = if(started) mockNetworkHelper.nodeHandles else null

}