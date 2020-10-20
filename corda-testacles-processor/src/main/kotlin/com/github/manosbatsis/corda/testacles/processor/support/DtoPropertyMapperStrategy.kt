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
package com.github.manosbatsis.corda.testacles.processor.support

import com.github.manotbatsis.kotlin.utils.kapt.dto.strategy.DtoMembersStrategy
import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement


interface DtoPropertyMapper {

    fun supportsSourceType(strategy: Class<*>): Boolean

}

interface DtoPropertyMapperStrategy {

    /** Level 1 filtering */
    fun supportsMapping(strategy: Class<*>): Boolean

    /** Level 1 filtering */
    fun supportsDtoStrategy(strategy: Class<*>): Boolean

    /** Level 2 filtering */
    fun supportsEnclosingTypeElement(typeElement: TypeElement): Boolean

    /** Level 3 filtering */
    fun supportsVariableElement(variableElement: VariableElement): Boolean
    fun toAltConstructorStatement(
            index: Int, variableElement: VariableElement, propertyName: String, propertyType: TypeName, commaOrEmpty: String
    ): DtoMembersStrategy.Statement?

    fun toPatchStatement(variableElement: VariableElement, commaOrEmpty: String): DtoMembersStrategy.Statement?
    fun toMapStatement(variableElement: VariableElement, commaOrEmpty: String): DtoMembersStrategy.Statement?
}
