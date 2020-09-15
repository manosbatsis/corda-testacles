/*
 * Corda Testacles: Test containers and tools to help cordapps grow.
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
package com.github.manosbatsis.corda.testacles.processor

import com.github.manosbatsis.corda.testacles.processor.support.ModelDtoStrategy
import com.github.manosbatsis.kotlin.utils.kapt.dto.strategy.DtoStrategy
import com.github.manosbatsis.kotlin.utils.kapt.processor.AbstractAnnotatedModelInfoProcessor
import com.github.manosbatsis.kotlin.utils.kapt.processor.AnnotatedElementInfo
import com.github.manosbatsis.kotlin.utils.kapt.processor.AnnotationProcessorBase
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion

/**
 * Kapt processor for generating DTOs etc,.
 */
@SupportedAnnotationTypes(
        "com.github.manosbatsis.corda.testacles.model.api.CordaTestaclesModelMixin")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(AnnotationProcessorBase.KAPT_OPTION_NAME_KAPT_KOTLIN_GENERATED)
class CordaTestaclesDtoAnnotationProcessor : AbstractAnnotatedModelInfoProcessor(
        primaryTargetRefAnnotationName = "primarySource",
        secondaryTargetRefAnnotationName = "secondarySource"
) {

    /** Get a list of strategies to apply per annotated element */
    private fun getDtoStrategies(annotatedElementInfo: AnnotatedElementInfo): Map<String, DtoStrategy> {
        return mapOf("Dto" to ModelDtoStrategy(annotatedElementInfo))
    }

    override fun processElementInfos(elementInfos: List<AnnotatedElementInfo>) =
            elementInfos.forEach { processElementInfo(it) }

    private fun processElementInfo(elementInfo: AnnotatedElementInfo) {
        println("processElementInfo, elementInfo: $elementInfo")
        getDtoStrategies(elementInfo).map { (suffix, strategy) ->

            val dtoStrategyBuilder = strategy.dtoTypeSpecBuilder()
            println("processElementInfo, suffix: ${suffix}")
            val dto = dtoStrategyBuilder.build()
            val packageName = elementInfo.generatedPackageName
            val fileName = "${elementInfo.primaryTargetTypeElementSimpleName}${suffix}"
            println("processElementInfo, dto: ${dto}")
            println("processElementInfo, packageName: ${packageName}")
            println("processElementInfo, fileName: ${fileName}")
            println("processElementInfo, sourceRootFile: ${sourceRootFile}")
            // Generate the Kotlin file
            getFileSpecBuilder(packageName, fileName)
                    .addType(dto)
                    .build()
                    .writeTo(sourceRootFile)
        }
    }

}


