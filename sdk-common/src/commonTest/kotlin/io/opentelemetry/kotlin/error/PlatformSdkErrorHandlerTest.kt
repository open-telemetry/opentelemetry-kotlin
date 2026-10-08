package io.opentelemetry.kotlin.error

import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun logsWithoutThrowing() {
        PlatformSdkErrorHandler.onError(SdkError.UserCodeError(exc, "export", SdkErrorSeverity.WARNING))
    }
}
