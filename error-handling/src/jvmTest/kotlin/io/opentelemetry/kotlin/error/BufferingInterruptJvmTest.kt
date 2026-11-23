package io.opentelemetry.kotlin.error

import kotlin.test.Test
import kotlin.test.assertTrue

internal class BufferingInterruptJvmTest {
    @Test
    fun testInterruptedDelegateRestoresFlag() {
        val handler = BufferingSdkErrorHandler()
        try {
            Thread.interrupted()
            assertTrue(handler.setDelegate { throw InterruptedException("stop") })
            handler.onError(SdkError.ApiMisuse("test", "test", SdkErrorSeverity.WARNING))
            assertTrue(Thread.currentThread().isInterrupted)
        } finally {
            Thread.interrupted()
        }
    }
}
