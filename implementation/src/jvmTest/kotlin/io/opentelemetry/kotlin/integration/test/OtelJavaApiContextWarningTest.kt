package io.opentelemetry.kotlin.integration.test

import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.init.useOtelJavaContextStorage
import io.opentelemetry.kotlin.toOtelJavaApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OtelJavaApiContextWarningTest {

    @Test
    fun testWarnsOncePerInstanceWithoutOptIn() {
        val errorHandler = FakeSdkErrorHandler()
        val otel = createOpenTelemetry { errorHandler(errorHandler) }
        otel.toOtelJavaApi()
        otel.toOtelJavaApi()

        val error = errorHandler.apiMisuses.single()
        assertEquals("toOtelJavaApi", error.api)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun testWarnsForEachInstance() {
        val errorHandler = FakeSdkErrorHandler()
        createOpenTelemetry { errorHandler(errorHandler) }.toOtelJavaApi()
        createOpenTelemetry { errorHandler(errorHandler) }.toOtelJavaApi()
        assertEquals(2, errorHandler.apiMisuses.size)
    }

    @Test
    fun testNoWarningWithOptIn() {
        val errorHandler = FakeSdkErrorHandler()
        createOpenTelemetry {
            errorHandler(errorHandler)
            context { useOtelJavaContextStorage() }
        }.toOtelJavaApi()
        assertTrue(errorHandler.apiMisuses.isEmpty())
    }
}
