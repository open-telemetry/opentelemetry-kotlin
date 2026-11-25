package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.ResourceBehavior
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.setProcessSdkErrorHandler
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ResourceBehaviorExtTest {

    private val errorHandler = FakeSdkErrorHandler()

    @BeforeTest
    fun setUp() {
        setProcessSdkErrorHandler(null)
        check(setProcessSdkErrorHandler(errorHandler))
    }

    @AfterTest
    fun tearDown() {
        setProcessSdkErrorHandler(null)
    }

    @Test
    fun testToResourceDropsEmptyKey() {
        val resource = ResourceBehavior(attributes = mapOf("" to "empty", "key" to "value")).toResource()
        assertEquals(mapOf("key" to "value"), resource.attributes)

        val error = errorHandler.apiMisuses.single()
        assertEquals("ResourceBehavior", error.api)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun testToResourceWithValidKeysIsNotReported() {
        val resource = ResourceBehavior(serviceName = "svc", attributes = mapOf("key" to "value")).toResource()
        assertEquals(mapOf("key" to "value", "service.name" to "svc"), resource.attributes)
        assertEquals(emptyList(), errorHandler.errors)
    }
}
