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
package com.github.manosbatsis.corda.testacles.model.api

import com.github.manotbatsis.kotlin.utils.api.DtoInsufficientMappingException
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party


interface ModelLiteDto<T : Any> : ModelBaseLiteDto<T, ModelAdapter<T>>

/**
 * Modeled after [com.github.manosbatsis.kotlin.utils.api.Dto]
 * only bringing a [ModelAdapter] in-context for [toTargetType] and [toPatched].
 */
interface ModelBaseLiteDto<T : Any, S : ModelAdapter<T>> {
    /**
     * Create a patched copy of the given [T] instance,
     * updated using this DTO's non-null properties
     */
    fun toPatched(original: T, ModelAdapter: S): T

    /**
     * Create an instance of [T], using this DTO's properties.
     * May throw a [DtoInsufficientMappingException]
     * if there is mot enough information to do so.
     */
    fun toTargetType(ModelAdapter: S): T

    fun toName(party: Party?, propertyName: String = "unknown"): CordaX500Name = party?.name
            ?: throw DtoInsufficientMappingException("Required property: $propertyName was null")

    fun toNameOrNull(party: Party?): CordaX500Name? = party?.name

}
