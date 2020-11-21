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
package com.github.manosbatsis.corbeans.test.containers

import com.typesafe.config.Config
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueFactory
import net.corda.nodeapi.internal.config.User
import org.testcontainers.containers.GenericContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.io.FileInputStream
import java.net.MalformedURLException
import java.net.URL


open class KImageNameContainer(
        dockerImageName: DockerImageName
) : GenericContainer<KImageNameContainer>(dockerImageName)

class KGenericContainer(
        dockerImage: ImageFromDockerfile
) : GenericContainer<KGenericContainer>(dockerImage)

fun disableTomcatURLStreamHandlerFactory(){
    // Stop Spring Boot's Tomcat (if present) from hijacking the URLStreamHandlerFactory implementation
    try {
        Class.forName("org.apache.catalina.webresources.TomcatURLStreamHandlerFactory")
                .getMethod("disable")
                .invoke(null)
    } catch (e: ClassNotFoundException) {
        print("Called disabpleTomcatURLStreamHandlerFactory but class TomcatURLStreamHandlerFactory is not present")
    }
}

object ConfigUtil {
    fun <T> valueFor(any: T): ConfigValue = ConfigValueFactory.fromAnyRef(any)

    fun getUsers(config: Config) =
            if (config.hasPath("rpcUsers")) {
                config.getConfigList("rpcUsers")
            } else {
                config.getConfigList("security.authService.dataSource.users")
            }.map {
                User(username = if (it.hasPath("username")) it.getString("username") else it.getString("user"),
                        password = it.getString("password"),
                        permissions = it.getStringList("permissions").toSet())
            }

}

object JarUtil{
    val fileUriMatchRegex = "file:[A-Za-z]:.*".toRegex()

    /**
     * Gets the base location of the given classname.
     *
     * If the class is directly on the file system (e.g.,
     * "/path/to/my/package/MyClass.class") then it will return the base directory
     * (e.g., "file:/path/to").
     *
     * If the class is within a JAR file (e.g.,
     * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
     * path to the JAR (e.g., "file:/path/to/my-jar.jar").
     *
     * @param classname The class qualified name whose location is desired.
     */
    fun getClassnameUrl(classname: String): URL? {

        // try the easy way first
        try {
            val codeSourceLocation = Class.forName(classname).protectionDomain.codeSource.location
            if (codeSourceLocation != null) return codeSourceLocation
        } catch (e: Exception) {
            // NB: Protection domain or code source is null.
        }

        // The easy way failed, so we try the hard way. We ask for the class
        // itself as a resource, then strip the class's path from the URL string,
        // leaving the base path.

        // get the class's raw resource path
        val classLocation = "${classname.replace(".", "/")}.class"

        val classResource = this.javaClass.classLoader.getResource(classLocation)
                ?: ClassLoader.getSystemClassLoader().getResource(classLocation)

        // Attempt to build a URL if resource was found,
        // return null otherwise
        return classResource?.run {
            // cannot find class resource
            val url = this.toString()
            if (!url.endsWith(".class")) return null // weird URL

            // strip the class's path from the URL string
            val base = url.substring(0, url.length - classLocation.length)
            var path = base

            // remove the "jar:" prefix and "!/" suffix, if present
            if (path.startsWith("jar:"))
                path = path.substring(4, path.length - 2)

            try {
                URL(path)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                null
            }
        }


    }

    /**
     * Converts the given URL string to its corresponding [File].
     *
     * @param urlString The URL to convert.
     * @return A file path suitable for use with e.g. [FileInputStream]
     * @throws IllegalArgumentException if the URL does not correspond to a file.
     */
    fun getClassnameFile(url: URL): File {
        var path = url.toString()
        if (path.startsWith("jar:")) {
            // remove "jar:" prefix and "!/" suffix
            val index = path.indexOf("!/")
            path = path.substring(4, index)
        }

        try {
            if (File.separatorChar == '\\' && path.matches(fileUriMatchRegex)) {
                path = "file:/" + path.substring(5)
                val file = File(URL(path).toURI())
                return file
            }
        } catch (e: Exception) {}
        if (path.startsWith("file:")) {
            // pass through the URL as-is, minus "file:" prefix
            path = path.substring(5)
            return File(path)
        }

        throw IllegalArgumentException("Invalid URL: $url.toString()")
    }
}