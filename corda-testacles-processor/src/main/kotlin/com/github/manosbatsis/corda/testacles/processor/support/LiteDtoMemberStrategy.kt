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

import co.paralleluniverse.fibers.Suspendable
import com.github.manosbatsis.corda.testacles.common.ModelAdapter
import com.github.manotbatsis.kotlin.utils.kapt.dto.strategy.DtoMembersStrategy.Statement
import com.github.manotbatsis.kotlin.utils.kapt.dto.strategy.DtoNameStrategy
import com.github.manotbatsis.kotlin.utils.kapt.dto.strategy.SimpleDtoMembersStrategy
import com.github.manotbatsis.kotlin.utils.kapt.processor.AnnotatedElementInfo
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec.Builder
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType

open class LiteDtoMemberStrategy(
        annotatedElementInfo: AnnotatedElementInfo,
        dtoNameStrategy: DtoNameStrategy,
        dtoTypeStrategy: DtoTypeStrategy
) : SimpleDtoMembersStrategy(
        annotatedElementInfo, dtoNameStrategy, dtoTypeStrategy
) {


    override fun toTargetTypeStatement(fieldIndex: Int, variableElement: VariableElement, commaOrEmpty: String): Statement? {
        return if (variableElement.asType().asTypeElement().asClassName() == Party::class.java.asClassName()) {

            val propertyName = toPropertyName(variableElement)
            if (variableElement.isNullable()) {
                targetTypeFunctionBuilder.addStatement("val ${propertyName}Resolved = toPartyOrNull(this.$propertyName, adapter, %S)", propertyName)
                Statement("      $propertyName = ${propertyName}Resolved$commaOrEmpty")
            } else {
                targetTypeFunctionBuilder.addStatement("val ${propertyName}Resolved = toParty(this.$propertyName, adapter, %S)", propertyName)
                Statement("      $propertyName = ${propertyName}Resolved$commaOrEmpty")
            }
        } else super.toTargetTypeStatement(fieldIndex, variableElement, commaOrEmpty)
    }

    override fun toPatchStatement(fieldIndex: Int, variableElement: VariableElement, commaOrEmpty: String): Statement? {
        return if (variableElement.asType().asTypeElement().asClassName() == Party::class.java.asClassName()) {
            val propertyName = toPropertyName(variableElement)
            if (variableElement.isNullable()) {
                patchFunctionBuilder.addStatement("val ${propertyName}Resolved = toPartyOrDefaultNullable(this.$propertyName, original.$propertyName, adapter, %S)", propertyName)
                Statement("      $propertyName = ${propertyName}Resolved$commaOrEmpty")
            } else {
                patchFunctionBuilder.addStatement("val ${propertyName}Resolved = toPartyOrDefault(this.$propertyName, original.$propertyName, adapter, %S)", arrayOf(propertyName))
                Statement("      $propertyName = ${propertyName}Resolved$commaOrEmpty")
            }
        } else if (isIterableOfParties(variableElement)) {
            val propertyName = toPropertyName(variableElement)
            if (variableElement.isNullable()) {
                patchFunctionBuilder.addStatement("val ${propertyName}Resolved = toPartiesOrDefaultNullable(this.$propertyName, original.$propertyName, adapter, %S)", propertyName)
                Statement("      $propertyName = ${propertyName}Resolved$commaOrEmpty")
            } else {
                patchFunctionBuilder.addStatement("val ${propertyName}Resolved = toPartiesOrDefault(this.$propertyName, original.$propertyName, adapter, %S)", arrayOf(propertyName))
                Statement("      $propertyName = ${propertyName}Resolved$commaOrEmpty")
            }
        } else super.toPatchStatement(fieldIndex, variableElement, commaOrEmpty)
    }

    override fun getCreatorFunctionBuilder(originalTypeParameter: ParameterSpec): FunSpec.Builder {
        val creator = super.getCreatorFunctionBuilder(originalTypeParameter)
                .addAnnotation(Suspendable::class.java)
        addModelAdapterParameter(creator)
        return creator
    }

    override fun getToPatchedFunctionBuilder(
            originalTypeParameter: ParameterSpec
    ): FunSpec.Builder {
        val functionBuilder = super.getToPatchedFunctionBuilder(originalTypeParameter)
                .addAnnotation(Suspendable::class.java)
        addModelAdapterParameter(functionBuilder)
        return functionBuilder
    }

    override fun toCreatorStatement(
            fieldIndex: Int, variableElement: VariableElement,
            propertyName: String, propertyType: TypeName,
            commaOrEmpty: String
    ): Statement? {
        return if (variableElement.asType().asTypeElement().asClassName() == Party::class.java.asClassName()) {
            Statement(
                    if (variableElement.isNullable())
                        "      $propertyName = original.$propertyName?.name$commaOrEmpty"
                    else "      $propertyName = original.$propertyName.name$commaOrEmpty"
            )
        } else if (isIterableOfParties(variableElement)) Statement(
                if (variableElement.isNullable())
                    "      $propertyName = original.$propertyName?.map{it.name}$commaOrEmpty"
                else "      $propertyName = original.$propertyName.map{it.name}$commaOrEmpty"
        )
        else return super.toCreatorStatement(fieldIndex, variableElement, propertyName, propertyType, commaOrEmpty)
    }

    override fun toAltConstructorStatement(
            fieldIndex: Int, variableElement: VariableElement,
            propertyName: String, propertyType: TypeName,
            commaOrEmpty: String
    ): Statement? {
        return if (variableElement.asType().asTypeElement().asClassName() == Party::class.java.asClassName()) {
            Statement(
                    if (variableElement.isNullable())
                        "      $propertyName = original.$propertyName?.name$commaOrEmpty"
                    else "      $propertyName = original.$propertyName.name$commaOrEmpty"
            )
        } else if (isIterableOfParties(variableElement)) {
            Statement(
                    if (variableElement.isNullable())
                        "      $propertyName = original.$propertyName?.map{it.name}$commaOrEmpty"
                    else "      $propertyName = original.$propertyName.map{it.name}$commaOrEmpty"
            )
        } else super.toAltConstructorStatement(fieldIndex, variableElement, propertyName, propertyType, commaOrEmpty)
    }

    // Create DTO alternative constructor
    override fun getAltConstructorBuilder(): FunSpec.Builder {
        val functionBuilder = super.getAltConstructorBuilder()
        addModelAdapterParameter(functionBuilder)
        return functionBuilder
    }

    override fun getToTargetTypeFunctionBuilder(): FunSpec.Builder {
        val functionBuilder = super.getToTargetTypeFunctionBuilder()
        addModelAdapterParameter(functionBuilder)
        return functionBuilder
    }

    open fun addModelAdapterParameter(functionBuilder: FunSpec.Builder) {
        functionBuilder.addParameter(
                "adapter",
                ModelAdapter::class.java.asClassName()
                        .parameterizedBy(annotatedElementInfo.primaryTargetTypeElement.asKotlinTypeName()))
    }

    override fun toPropertyTypeName(variableElement: VariableElement): TypeName {
        return if (variableElement.asType().asTypeElement().asClassName() == Party::class.java.asClassName())
            CordaX500Name::class.java.asTypeName().copy(nullable = true)
        else if (isIterableOfParties(variableElement)) processingEnvironment.typeUtils
                .erasure(variableElement.asType())
                .asTypeElement().asKotlinClassName()
                .parameterizedBy(CordaX500Name::class.java.asTypeName())
        else super.toPropertyTypeName(variableElement)

    }

    fun isIterableOfParties(variableElement: VariableElement): Boolean {
        val variableType = variableElement.asType()
        val variableTypeArg = if (variableType is DeclaredType)
            variableType.typeArguments.firstOrNull()
        else null
        return variableType.asTypeElement().isSunTypeOf(Iterable::class.java, true)
                && variableTypeArg != null
                && variableTypeArg.asTypeElement().isAssignableTo(Party::class.java)

    }

    override fun addAltConstructor(typeSpecBuilder: Builder, dtoAltConstructorBuilder: FunSpec.Builder) {
        // NO-OP
    }
}
