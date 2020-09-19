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
package com.github.manosbatsis.corda.testacles.containers.cordform

import org.apache.commons.io.FileUtils
import java.io.File


data class FsBindDirs(
        val nodesDir: File,
        val netParamsFile: File = File(nodesDir, "network-parameters"),
        val nodeInfosDir: File = File(nodesDir, "additional-node-infos").apply { mkdirs() }
){

    private val allSubDirs = nodesDir.listFiles { file ->
        file.isDirectory && File(file, "node.conf").exists()
    }.takeIf { it.isNotEmpty() } ?: error("Could not find any node directories in ${nodesDir.absolutePath}")

    val notaryNodeDirs: List<File>
        get() = allSubDirs.filter { it.name.contains("notary", true) }
    val partyNodeDirs: List<File>
        get() = allSubDirs.filter { !it.name.contains("notary", true) }

    val nodeDirs: List<File>
        get() = notaryNodeDirs + partyNodeDirs

    init {
        FileUtils.copyFile(
                File(nodeDirs.first(), "network-parameters"),
                netParamsFile)
    }
}
