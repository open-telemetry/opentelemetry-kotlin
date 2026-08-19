package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class TelemetryExceptionHandlerTest {

    @Test
    fun testCoroutineFailureReportsUserCodeError() {
        val handler = FakeSdkErrorHandler()
        val cause = IllegalStateException("boom")

        telemetryExceptionHandler("Test context", handler)
            .handleException(EmptyCoroutineContext, cause)

        assertEquals(1, handler.userCodeErrors.size)
        val error = handler.userCodeErrors.single()
        assertEquals("Test context coroutine failed", error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
        assertIs<IllegalStateException>(error.cause)
        assertEquals("boom", error.cause.message)
    }
}
