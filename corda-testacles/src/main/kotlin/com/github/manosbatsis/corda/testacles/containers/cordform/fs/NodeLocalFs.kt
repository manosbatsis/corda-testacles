/*
 * Corda Testacles: Tools to grow some cordapp test suites.
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
package com.github.manosbatsis.corda.testacles.containers.cordform.fs

import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion.NODE_CONF_FILENAME_CUSTOM
import com.github.manosbatsis.corda.testacles.containers.node.NodeContainer.Companion.NODE_CONF_FILENAME_DEFAULT
import java.io.File

data class NodeLocalFs(
        val nodeDir: File,
        val nodeHostName: String,
        val nodeConfFile: File = resolveConfig(nodeDir),
        val netParamsFile: File? = null,
        val nodeInfosDir: File? = null
){
    companion object{
        /**
         * Looks for a config file in the node directory,
         * first checking for [NODE_CONF_FILENAME_CUSTOM],
         * then falling back to [NODE_CONF_FILENAME_DEFAULT] \
         */
        fun resolveConfig(nodeDir: File): File {
            return listOf(NODE_CONF_FILENAME_CUSTOM, NODE_CONF_FILENAME_DEFAULT)
                    .map { nodeDir.resolve(it) }
                    .find { it.exists() }
                    ?: throw IllegalArgumentException("Input file must be a node.conf or node directory")
        }

    }
}