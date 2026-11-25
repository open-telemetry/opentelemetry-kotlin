package io.opentelemetry.kotlin.error

import kotlin.test.Test

internal class PlatformSdkErrorHandlerJvmTest {

    @Test
    fun toleratesHostileThrowable() {
        PlatformSdkErrorHandler.onError(
            SdkError.UserCodeError(HostileThrowable(), "export", SdkErrorSeverity.WARNING),
        )
    }

    @Suppress("ExceptionRaisedInUnexpectedLocation")
    private class HostileThrowable : Throwable() {
        override val message: String
            get() = throw AssertionError("message accessed")

        override fun getStackTrace(): Array<StackTraceElement> =
            throw AssertionError("stack trace accessed")

        override fun toString(): String = throw AssertionError("toString called")
    }
}
