package com.github.manosbatsis.corda.testacles.mocknetwork.config

/**
 * Wraps a list of one or more strings with each one
 * being an organization or X500 name.
 *
 * For entries that are simple organization names (i.e. not X500 names),
 * a random locality/country will be selected during conversion to [CordaX500Name].
 */
class OrgNames(private val names: List<String>): List<String> by names {
    init {
        if(names.isEmpty()) throw IllegalArgumentException("Parameter names cannot be empty")
    }
}