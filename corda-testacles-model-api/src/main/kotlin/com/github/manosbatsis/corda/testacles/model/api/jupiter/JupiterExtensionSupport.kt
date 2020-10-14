/*
 * Corda Testacles: Test suite toolkit for Corda developers.
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
package com.github.manosbatsis.corda.testacles.model.api.jupiter

import net.corda.core.internal.isStatic
import org.junit.jupiter.api.extension.ExtensionConfigurationException
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.util.Preconditions
import java.lang.reflect.Field

interface JupiterExtensionSupport{

    fun isAnnotatedWithAndOfType(
            field: Field, annotation: Class<out Annotation>, fieldType: Class<*>
    ): Boolean {
        val isAnnotatedWithNodesDir = field.isAnnotationPresent(annotation)
        return if (isAnnotatedWithNodesDir) {
            if (fieldType.isAssignableFrom(field.type)) true
            else throw ExtensionConfigurationException(
                    "Field: ${field.name} is not a ${fieldType.simpleName} " +
                            "object annotated with ${annotation.simpleName}")
        } else false
    }

    fun <T> getFieldValue(
            testInstance: Any?, field: Field
    ): T? = try {
        field.isAccessible = true
        Preconditions.notNull(field.get(testInstance) as T, "Container " + field.name + " needs to be initialized")
    } catch (e: IllegalAccessException) {
        throw ExtensionConfigurationException("Can not access container defined in field " + field.name)
    }

    fun <T: Class<*>, A: Class<out Annotation>, F> findNAnnotatedFieldValue(
            testClass: T,
            annotationClass: A,
            fieldClass: Class<F>
    ): F? = testClass.declaredFields.filter {
                it.isStatic && isAnnotatedWithAndOfType(it, annotationClass, fieldClass)
            }
            .map { f: Field -> getFieldValue<F>(null, f) }
            .singleOrNull()


    fun getRequiredTestClass(context: ExtensionContext): Class<*> =
            context.testClass.orElseThrow {
                ExtensionConfigurationException("Extension is only supported for classes.") }


}