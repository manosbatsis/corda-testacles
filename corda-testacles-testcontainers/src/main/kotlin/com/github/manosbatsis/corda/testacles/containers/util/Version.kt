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
package com.github.manosbatsis.corda.testacles.containers.util


class Version(val version: String) : Comparable<Version?> {
    companion object{
        val validationRegex = Regex("[0-9]+(\\.[0-9]+)*")
    }

    init {
        if (!version.matches(validationRegex)) throw IllegalArgumentException("Invalid version format")
    }

    override fun toString(): String {
        return version
    }


    override fun compareTo(other: Version?): Int {
        return if(other != null) versionCompare(this.version, other.version)
        else throw NullPointerException("")
    }

    private fun versionCompare(left: String, right: String): Int {
        val leftSegments = left.split('.').toMutableList()
        val rightSegments = right.split('.').toMutableList()
        while (leftSegments.size != rightSegments.size) {
            if (leftSegments.size < rightSegments.size) {
                leftSegments.add("0")
            } else {
                rightSegments.add("0")
            }
        }
        var i = 0
        while (i < leftSegments.size && i < rightSegments.size && leftSegments[i] == rightSegments[i]) {
            i++
        }
        return if (i < leftSegments.size && i < rightSegments.size) {
            val diff = Integer.valueOf(leftSegments[i]).compareTo(Integer.valueOf(rightSegments[i]))
            Integer.signum(diff)
        } else {
            Integer.signum(leftSegments.size - rightSegments.size)
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        return if (this.javaClass != other.javaClass) false else this.compareTo(other as Version) == 0
    }



}