package com.github.manosbatsis.corda.testacles.model.api

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class MapToInstanceHelper {
    companion object{
        fun <T: Any> instance(targetClass: KClass<T>, parameters: Map<String,Any?>):T{
            val primaryConstructor = targetClass.primaryConstructor!!
            val constructorParameters = primaryConstructor.parameters.associateBy({it},{ parameters[it.name] })
            return primaryConstructor.callBy(constructorParameters)
        }
    }
}