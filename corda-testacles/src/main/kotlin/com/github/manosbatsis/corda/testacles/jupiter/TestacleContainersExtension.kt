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
package com.github.manosbatsis.corda.testacles.jupiter

import com.github.manosbatsis.corda.testacles.Testacle
import com.github.manosbatsis.corda.testacles.TestacleContainers
import com.github.manosbatsis.corda.testacles.jupiter.support.FilesystemFriendlyNameGenerator
import com.github.manosbatsis.corda.testacles.jupiter.support.Startables
import com.github.manosbatsis.corda.testacles.jupiter.support.StoreAdapter
import com.github.manosbatsis.corda.testacles.jupiter.support.TestcontainersTestDescription
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionConfigurationException
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.platform.commons.logging.LoggerFactory
import org.junit.platform.commons.util.Preconditions
import org.junit.platform.commons.util.ReflectionUtils
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.TOP_DOWN
import org.testcontainers.DockerClientFactory
import org.testcontainers.lifecycle.TestDescription
import org.testcontainers.lifecycle.TestLifecycleAware
import java.lang.reflect.Field
import java.util.Optional
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream


open class TestacleContainersExtension : BeforeEachCallback, BeforeAllCallback, AfterEachCallback, AfterAllCallback, ExecutionCondition, TestInstancePostProcessor {


    companion object {

        @JvmStatic
        private val logger = LoggerFactory.getLogger(TestacleContainersExtension::class.java)

        protected val NAMESPACE: Namespace = Namespace.create(TestacleContainersExtension::class.java)
        protected const val TEST_INSTANCE = "testInstance"
        protected const val SHARED_LIFECYCLE_AWARE_CONTAINERS = "sharedLifecycleAwareContainers"
        protected const val LOCAL_LIFECYCLE_AWARE_CONTAINERS = "localLifecycleAwareContainers"
        protected val isContainer: Predicate<Field>
            protected get() = Predicate { field: Field ->
                field.isAnnotationPresent(Testacle::class.java)
            }

        protected fun getContainerInstances(testInstance: Any?, field: Field): List<StoreAdapter> {
            val instances = return try {
                field.isAccessible = true
                val containerInstances = Preconditions.notNull(field[testInstance] as Startables, "Container " + field.name + " needs to be initialized")
                containerInstances.startables.mapIndexed { index, startable ->
                    logger.info{"getContainerInstances, adding instance,  field: ${field.name}, index: $index, startable: ${startable}"}
                    StoreAdapter(field.declaringClass, "${field.name}", index, startable)
                }
            } catch (e: IllegalAccessException) {
                throw ExtensionConfigurationException("Can not access containers defined in field " + field.name)
            }
            logger.info{"getContainerInstances, field: ${field.name}, instances: ${instances}"}
            return instances
        }
    }
    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        val store = context.getStore(NAMESPACE)
        store.put(TEST_INSTANCE, testInstance)
    }

    override fun beforeAll(context: ExtensionContext) {
        val testClass = context.testClass
                .orElseThrow { ExtensionConfigurationException("TestacleContainersExtension is only supported for classes.") }
        logger.info{"beforeAll, testClass: ${testClass.name}"}
        val store = context.getStore(NAMESPACE)
        val sharedContainersStoreAdapters = findSharedContainers(testClass)
        logger.info{"beforeAll, sharedContainersStoreAdapters count: ${sharedContainersStoreAdapters.size}"}
        sharedContainersStoreAdapters.forEach(Consumer { adapter: StoreAdapter ->
            logger.info{"beforeAll, starting adapter: ${adapter.key}"}
            store.getOrComputeIfAbsent(adapter.key, Function { k: String? -> adapter.start() })
        })
        val lifecycleAwareContainers = sharedContainersStoreAdapters
                .stream()
                .filter { adapter: StoreAdapter -> isTestLifecycleAware(adapter) }
                .map { lifecycleAwareAdapter: StoreAdapter -> lifecycleAwareAdapter.container as TestLifecycleAware }
                .collect(Collectors.toList())
        store.put(SHARED_LIFECYCLE_AWARE_CONTAINERS, lifecycleAwareContainers)
        signalBeforeTestToContainers(lifecycleAwareContainers, testDescriptionFrom(context))
    }

    override fun afterAll(context: ExtensionContext) {
        logger.info{"afterAll, context: ${context}"}
        signalAfterTestToContainersFor(SHARED_LIFECYCLE_AWARE_CONTAINERS, context)
    }

    override fun beforeEach(context: ExtensionContext) {
        logger.info{"beforeEach, context: ${context}"}
        val store = context.getStore(NAMESPACE)
        val lifecycleAwareContainers: List<TestLifecycleAware> = collectParentTestInstances(context).parallelStream()
                .flatMap { testInstance: Any -> findRestartContainers(testInstance) }
                .peek(Consumer { adapter: StoreAdapter -> store.getOrComputeIfAbsent(adapter.key, Function { k: String? -> adapter.start() }) })
                .filter(Predicate { adapter: StoreAdapter -> isTestLifecycleAware(adapter) })
                .map(Function { lifecycleAwareAdapter: StoreAdapter -> lifecycleAwareAdapter.container as TestLifecycleAware })
                .collect(Collectors.toList())
        store.put(LOCAL_LIFECYCLE_AWARE_CONTAINERS, lifecycleAwareContainers)
        signalBeforeTestToContainers(lifecycleAwareContainers, testDescriptionFrom(context))
    }

    override fun afterEach(context: ExtensionContext) {
        logger.info{"afterEach, context: ${context}"}
        signalAfterTestToContainersFor(LOCAL_LIFECYCLE_AWARE_CONTAINERS, context)
    }

    protected fun signalBeforeTestToContainers(lifecycleAwareContainers: List<TestLifecycleAware>, testDescription: TestDescription?) {
        lifecycleAwareContainers.forEach(Consumer { container: TestLifecycleAware -> container.beforeTest(testDescription) })
    }

    protected fun signalAfterTestToContainersFor(storeKey: String, context: ExtensionContext) {
        val lifecycleAwareContainers = context.getStore(NAMESPACE)[storeKey] as List<TestLifecycleAware>
        if (lifecycleAwareContainers != null) {
            val description = testDescriptionFrom(context)
            val throwable = context.executionException
            lifecycleAwareContainers.forEach(Consumer { container: TestLifecycleAware -> container.afterTest(description, throwable) })
        }
    }

    protected fun testDescriptionFrom(context: ExtensionContext): TestDescription {
        return TestcontainersTestDescription(
                context.uniqueId,
                FilesystemFriendlyNameGenerator.filesystemFriendlyNameOf(context)
        )
    }

    protected fun isTestLifecycleAware(adapter: StoreAdapter): Boolean {
        return adapter.container is TestLifecycleAware
    }

    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult {
        return findTestacleContainers(context).map { testacleContainers: TestacleContainers -> evaluate(testacleContainers) }
                .orElseThrow { ExtensionConfigurationException("@TestacleContainers not found") }
    }

    protected fun findTestacleContainers(context: ExtensionContext?): Optional<TestacleContainers> {
        var current: Optional<ExtensionContext> = Optional.ofNullable(context)
        logger.info{"findTestacleContainers, context: ${context}"}
        while (current.isPresent) {

            val requiredTestClass = current.get().requiredTestClass
            logger.info{"findTestacleContainers, requiredTestClass: ${requiredTestClass}"}
            val testacleContainers = Optional.ofNullable(requiredTestClass.getAnnotation(TestacleContainers::class.java))

            logger.info{"findTestacleContainers, testacleContainers: ${testacleContainers}"}
            //val TestacleContainers = AnnotationUtils.findAnnotation(current.get().requiredTestClass, TestacleContainers::class.java)
            if (testacleContainers.isPresent) {
                return testacleContainers
            }
            current = current.get().parent
        }
        return Optional.empty()
    }

    protected fun evaluate(testacleContainers: TestacleContainers): ConditionEvaluationResult {
        logger.info{"evaluate, testacleContainers: ${testacleContainers}"}
        return if (testacleContainers.disabledWithoutDocker) {
            if (isDockerAvailable) {
                ConditionEvaluationResult.enabled("Docker is available")
            } else ConditionEvaluationResult.disabled("disabledWithoutDocker is true and Docker is not available")
        } else ConditionEvaluationResult.enabled("disabledWithoutDocker is false")
    }

    val isDockerAvailable: Boolean
        get() = try {
            DockerClientFactory.instance().client()
            true
        } catch (ex: Throwable) {
            false
        }

    protected fun collectParentTestInstances(context: ExtensionContext?): Set<Any> {
        logger.info{"collectParentTestInstances, context: ${context}"}
        val testInstances: MutableSet<Any> = LinkedHashSet()
        var current: Optional<ExtensionContext> = Optional.ofNullable(context)
        while (current.isPresent) {
            val ctx = current.get()
            val testInstance = ctx.getStore(NAMESPACE).remove(TEST_INSTANCE)
            if (testInstance != null) {
                testInstances.add(testInstance)
            }
            current = ctx.parent
        }
        return testInstances
    }

    protected open fun findSharedContainers(testClass: Class<*>): List<StoreAdapter> {
        logger.info{"findSharedContainers, testClass: ${testClass.name}"}
        val sharedContainers = ReflectionUtils.findFields(
                testClass,
                isSharedContainer,
                TOP_DOWN)
                .map { f: Field -> getContainerInstances(null, f) }
                .flatten()
        logger.info{"findSharedContainers, sharedContainers: ${sharedContainers}"}
        return sharedContainers

    }

    protected open val isSharedContainer: Predicate<Field>
        protected get() = isContainer.and { member: Field -> ReflectionUtils.isStatic(member) }

    protected open fun findRestartContainers(testInstance: Any): Stream<StoreAdapter> {
        logger.info{"findRestartContainers, testClass: ${testInstance}"}
        val sharedContainers = ReflectionUtils.findFields(
                testInstance.javaClass,
                isRestartContainer,
                TOP_DOWN)
                .map { f: Field -> getContainerInstances(testInstance, f) }
                .flatten()
        logger.info{"findRestartContainers, sharedContainers: ${sharedContainers}"}
        return sharedContainers.stream()
    }

    protected open val isRestartContainer: Predicate<Field>
        protected get() = isContainer.and { member: Field -> ReflectionUtils.isNotStatic(member) }


}
