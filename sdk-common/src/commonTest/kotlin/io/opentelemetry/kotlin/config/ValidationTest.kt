package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ValidationTest {

    private lateinit var handler: FakeSdkErrorHandler

    @BeforeTest
    fun setUp() {
        handler = FakeSdkErrorHandler()
    }

    @Test
    fun returnsValueAndReportsNothingWhenValidatorAccepts() {
        val result = validateOrUseDefault(
            sdkErrorHandler = handler,
            api = "TestApi",
            configParameterName = "size",
            value = 10,
            default = 100,
        ) { it > 0 }
        assertEquals(10, result)
        assertTrue(handler.apiMisuses.isEmpty())
    }

    @Test
    fun returnsDefaultAndReportsMisuseWhenValidatorRejects() {
        val result = validateOrUseDefault(
            sdkErrorHandler = handler,
            api = "TestApi",
            configParameterName = "size",
            value = -1,
            default = 100,
        ) { it > 0 }
        assertEquals(100, result)
        assertEquals(1, handler.apiMisuses.size)
        with(handler.apiMisuses.single()) {
            assertEquals("TestApi", api)
            assertEquals(SdkErrorSeverity.WARNING, severity)
        }
    }
}
