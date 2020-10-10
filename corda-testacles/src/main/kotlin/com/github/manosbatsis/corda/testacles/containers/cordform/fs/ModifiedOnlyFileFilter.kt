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

import java.io.File
import java.io.FileFilter
import java.nio.file.Path

/**
 * A [FileFilter] to only copy files
 * more recent than their destination
 */
class ModifiedOnlyFileFilter(
        val sourceDirPath: Path,
        val destDirPath: Path
): FileFilter {
    constructor(sourceDir: File, destinationDir: File):
            this(sourceDirPath = sourceDir.toPath(), destDirPath = destinationDir.toPath())

    override fun accept(pathname: File): Boolean {
        val relativePath = sourceDirPath.relativize(pathname.toPath())
        val targetFile = destDirPath.resolve(relativePath).toFile()
        return when{
            !targetFile.exists() -> true
            pathname.lastModified() > targetFile.lastModified() -> true
            else -> false
        }
    }
}