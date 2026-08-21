package io.opentelemetry.kotlin.error

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class GuardedSdkErrorHandlerTest {

    @Test
    fun forwardsReportsToTheDelegate() {
        val delegate = FakeSdkErrorHandler()
        val error = sdkError("first")

        GuardedSdkErrorHandler(delegate).onError(error)

        assertSame(error, delegate.errors.single())
    }

    @Test
    fun swallowsThrowablesFromTheDelegate() {
        val handler = GuardedSdkErrorHandler { throw IllegalStateException("boom") }

        handler.onError(sdkError("first"))
    }

    @Test
    fun keepsForwardingAfterTheDelegateThrows() {
        val delegate = FakeSdkErrorHandler()
        val handler = GuardedSdkErrorHandler {
            delegate.onError(it)
            if (delegate.errors.size == 1) {
                error("boom")
            }
        }

        handler.onError(sdkError("first"))
        handler.onError(sdkError("second"))

        assertEquals(listOf("first", "second"), delegate.apiMisuses.map(SdkError.ApiMisuse::api))
    }

    private fun sdkError(api: String) = SdkError.ApiMisuse(api, "boom", SdkErrorSeverity.WARNING)
}
