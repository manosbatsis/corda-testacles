package com.github.manosbatsis.corda.testacles.mocknetwork.config

import net.corda.core.identity.CordaX500Name

/**
 * Wraps a list of one or more [CordaX500Name]s */
class CordaX500Names(private val names: List<CordaX500Name>): List<CordaX500Name> by names {
    init {
        if(names.isEmpty()) throw IllegalArgumentException("Parameter names cannot be empty")
    }
}