package io.opentelemetry.kotlin.error

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PlatformSdkErrorHandlerTest {

    private val exc = IllegalStateException("boom")

    @Test
    fun formatsLogMessages() {
        val misuse = SdkError.ApiMisuse("Tracer.foo", "bad", SdkErrorSeverity.WARNING)
        assertEquals("Tracer.foo misused: bad", misuse.toLogMessage())
        val userCode = SdkError.UserCodeError(exc, "export", SdkErrorSeverity.ERROR)
        assertEquals("User code failed: export", userCode.toLogMessage())
        val sdkCode = SdkError.SdkCodeError(exc, "read", SdkErrorSeverity.ERROR)
        assertEquals("SDK code failed: read", sdkCode.toLogMessage())
    }

    @Test
    fun mapsEachSeverityToSameNamedLogLevel() {
        SdkErrorSeverity.entries.forEach { severity ->
            assertEquals(severity.name, severity.toPlatformLogLevel().name)
        }
    }

    @Test
    fun logsWithoutThrowing() {
        PlatformSdkErrorHandler.onError(SdkError.UserCodeError(exc, "export", SdkErrorSeverity.WARNING))
        PlatformSdkErrorHandler.onError(SdkError.SdkCodeError(exc, "export", SdkErrorSeverity.ERROR))
        PlatformSdkErrorHandler.onError(SdkError.ApiMisuse("Tracer.foo", "bad", SdkErrorSeverity.INFO))
    }

    @Test
    fun retriesWithoutCauseWhenLoggingFails() {
        verifyRetry("export failed", exc)
    }

    private fun verifyRetry(message: String, cause: Throwable) {
        val messages = mutableListOf<String>()
        var attempts = 0
        io.opentelemetry.kotlin.error.safelyPlatformLog(
            message,
            io.opentelemetry.kotlin.PlatformLogLevel.WARNING,
            cause,
        ) { loggedMessage, _ ->
            messages += loggedMessage
            if (attempts++ == 0) {
                error("logger failed")
            }
        }

        assertEquals(2, messages.size)
        assertTrue(messages.first().startsWith("$message\n"))
        assertEquals(message, messages.last())
    }

    @Test
    fun swallowsFailureWhenRetryAlsoFails() {
        var attempts = 0

        io.opentelemetry.kotlin.error.safelyPlatformLog(
            "export failed",
            io.opentelemetry.kotlin.PlatformLogLevel.ERROR,
            exc,
        ) { _, _ ->
            attempts++
            error("logger failed")
        }

        assertEquals(2, attempts)
    }

    @Test
    fun doesNotRetryWithoutCause() {
        var attempts = 0

        io.opentelemetry.kotlin.error.safelyPlatformLog(
            "bad API use",
            io.opentelemetry.kotlin.PlatformLogLevel.WARNING,
            logger = { _, _ ->
                attempts++
                error("logger failed")
            },
        )

        assertEquals(1, attempts)
    }
}
