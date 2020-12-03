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

import com.github.manosbatsis.corda.testacles.common.ModelLiteDto
import com.github.manotbatsis.kotlin.utils.kapt.processor.AnnotatedElementInfo
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec.Builder
import com.squareup.kotlinpoet.asClassName

open class LiteDtoTypeStrategy(
        annotatedElementInfo: AnnotatedElementInfo
) : DtoTypeStrategy(annotatedElementInfo) {

    override fun addSuperTypes(typeSpecBuilder: Builder) {
        val typeName = annotatedElementInfo.primaryTargetTypeElement.asKotlinTypeName()
        typeSpecBuilder.addSuperinterface(
                ModelLiteDto::class.asClassName().parameterizedBy(typeName))
    }
}
