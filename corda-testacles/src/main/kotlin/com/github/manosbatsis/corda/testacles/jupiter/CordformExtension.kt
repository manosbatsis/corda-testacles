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
package com.github.manosbatsis.corda.testacles.jupiter

import com.github.manosbatsis.corbeans.test.containers.KImageNameContainer
import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNodesContainer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.support.AnnotationSupport
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Network
import java.io.File


open class CordformExtension: BeforeAllCallback, AfterAllCallback {

    companion object {
        private val logger = LoggerFactory.getLogger(CordformExtension::class.java)
    }

    lateinit var cordformNodesContainer: CordformNodesContainer
    var instances: MutableMap<String,   KImageNameContainer> = mutableMapOf()
    var instancePorts: MutableMap<String, Int> = mutableMapOf()

    /**
     * Get the nodes dir (probably created by cordform) for the test in context.
     * This implementation will search the current test suite for a [File] field annotated
     * with [NodesDir], then fallback to _./build/nodes_ if none is found.
     */
    protected open fun getTestNodesDir(context: ExtensionContext): File{
        val nodesDir: File = AnnotationSupport
                .findAnnotatedFields(context.testInstance.javaClass, NodesDir::class.java)
                .singleOrNull()
                ?.get(context.testInstance) as File?
                ?: File("user.dir").resolve("build/nodes")
        if(!nodesDir.exists() || !nodesDir.isDirectory)
            error("Specified nodes dir must exist as a directory: ${nodesDir.absolutePath}")
        return nodesDir
    }
    
    /** Create and start the Corda network */
    override fun beforeAll(context: ExtensionContext) {
        if (instances.isNotEmpty()) error("Method startContainers called but instances are already initialized")
        cordformNodesContainer = CordformNodesContainer(
                nodesDir = getTestNodesDir(context),
                network = Network.newNetwork())
        cordformNodesContainer.start()

    }

    /** Stop the Corda network */
    override fun afterAll(context: ExtensionContext) {
        cordformNodesContainer.stop()
    }

}
