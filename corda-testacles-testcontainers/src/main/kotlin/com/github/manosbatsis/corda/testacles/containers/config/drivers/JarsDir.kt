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
package com.github.manosbatsis.corda.testacles.containers.config.drivers

import com.github.manosbatsis.corda.testacles.containers.util.JarUtil
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.Enumeration
import java.util.jar.JarEntry
import java.util.jar.JarFile


class JarsDir(
        val location: File
) {
    companion object{
        val jarFileEntryPathSeparatorMatcher = "/|\\\\".toRegex()
    }

    init {
        if (!location.exists()) location.mkdirs()
        require(location.isDirectory) { "Given location is not a directory: ${location.absolutePath}" }
    }

    private val classnameJars: MutableMap<String, String> = mutableMapOf()

    fun resolveClassJarFilename(classname: String): String {
        return classnameJars.getOrPut(classname) {
            val jarFile: File = getJarForClass(classname)
                    ?: addJarForClass(classname)
                    ?: error("JAR for class $classname must be in location dir " +
                            "or the classpath")

            jarFile.name
        }
    }

    private fun addJarForClass(classname: String): File? {
        return JarUtil.getClassnameUrl(classname)
                ?.run { JarUtil.getClassnameFile(this) }
                ?.also { FileUtils.copyFileToDirectory(it, location) }
    }

    private fun getJarForClass(classname: String): File? {
        return location
                .listFiles { dir, name -> name.endsWith(".jar") }
                .toList()
                .find {
                    val jar = JarFile(it)
                    val en: Enumeration<JarEntry> = jar.entries()
                    var found = false
                    while (en.hasMoreElements() && !found) {
                        found = classname == jarEntryToClassName(en.nextElement())
                    }
                    found
                }
    }
    private fun jarEntryToClassName(entry: JarEntry): String? {
        return if (!entry.name.endsWith("class")) null
        else entry.name.substring(0, entry.name.length - 6)
                .replace(jarFileEntryPathSeparatorMatcher, "\\.")
    }
}