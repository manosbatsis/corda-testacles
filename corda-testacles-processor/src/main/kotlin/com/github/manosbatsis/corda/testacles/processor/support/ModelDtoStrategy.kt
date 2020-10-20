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

import com.github.manosbatsis.kotlin.utils.ProcessingEnvironmentAware
import com.github.manotbatsis.kotlin.utils.kapt.dto.strategy.CompositeDtoStrategy
import com.github.manotbatsis.kotlin.utils.kapt.dto.strategy.DtoStrategyComposition
import com.github.manotbatsis.kotlin.utils.kapt.dto.strategy.SimpleDtoNameStrategy
import com.github.manotbatsis.kotlin.utils.kapt.processor.AnnotatedElementInfo
import javax.lang.model.element.VariableElement

/** Custom overrides for building a DTO type spec */
open class ModelDtoStrategy(
        annotatedElementInfo: AnnotatedElementInfo,
        composition: DtoStrategyComposition =
                ModelDefaultDtoStrategyComposition(annotatedElementInfo)
) : CompositeDtoStrategy(
        annotatedElementInfo, composition
), ProcessingEnvironmentAware, AnnotatedElementInfo by annotatedElementInfo {


    override fun getFieldsToProcess(): List<VariableElement> {
        processingEnvironment.noteMessage { "\nModelDtoStrategy.getFieldsToProcess, ignoreProperties: $ignoreProperties" }
        processingEnvironment.noteMessage { "\nModelDtoStrategy.getFieldsToProcess, ignored: $ignoreProperties" }
        return primaryTargetTypeElementFields.filterNot { ignoreProperties.contains(it.simpleName.toString()) }
                .map {
                    processingEnvironment.noteMessage { "\nModelDtoStrategy.getFieldsToProcess, includiong: ${it.simpleName}" }
                    it
                }
    }
}

/** Project-specific overrides for building a DTO type spec */
open class DefaultDtoStrategy(
        annotatedElementInfo: AnnotatedElementInfo
) : ModelDtoStrategy(
        annotatedElementInfo = annotatedElementInfo,
        composition = ModelDefaultDtoStrategyComposition(annotatedElementInfo)
)

/** Project-specific overrides for building a "lite" DTO type spec */
class LiteDtoStrategy(
        annotatedElementInfo: AnnotatedElementInfo
) : ModelDtoStrategy(
        annotatedElementInfo = annotatedElementInfo,
        composition = ModelLiteDtoStrategyComposition(annotatedElementInfo)
)

open class ModelDefaultDtoStrategyComposition(
        override val annotatedElementInfo: AnnotatedElementInfo
) : DtoStrategyComposition {

    override val dtoNameStrategy = SimpleDtoNameStrategy(annotatedElementInfo)
    override val dtoTypeStrategy = DtoTypeStrategy(annotatedElementInfo)
    override val dtoMembersStrategy = ModelDtoMemberStrategy(
            annotatedElementInfo, dtoNameStrategy, dtoTypeStrategy)
}

open class ModelLiteDtoStrategyComposition(
        override val annotatedElementInfo: AnnotatedElementInfo
) : DtoStrategyComposition {

    override val dtoNameStrategy = LiteDtoNameStrategy(annotatedElementInfo)
    override val dtoTypeStrategy = LiteDtoTypeStrategy(annotatedElementInfo)
    override val dtoMembersStrategy = LiteDtoMemberStrategy(
            annotatedElementInfo, dtoNameStrategy, dtoTypeStrategy)
}
