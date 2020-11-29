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


    override fun equals(that: Any?): Boolean {
        if (this === that) return true
        if (that == null) return false
        return if (this.javaClass != that.javaClass) false else this.compareTo(that as Version) == 0
    }



}