package io.opentelemetry.kotlin.error

import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * Records errors reported to the process-wide default [SdkErrorHandler]. Call [install] before
 * the code under test runs and [uninstall] after it.
 */
internal class RecordingSdkErrorHandler : SdkErrorHandler {

    val errors = mutableListOf<SdkError>()

    override fun onError(error: SdkError) {
        errors += error
    }

    /**
     * Discards errors buffered by earlier tests, then sets this handler as the delegate.
     */
    fun install() {
        setProcessSdkErrorHandler(null)
        check(setProcessSdkErrorHandler(this))
    }

    fun uninstall() {
        setProcessSdkErrorHandler(null)
    }

    /**
     * Asserts that exactly one [SdkError.UserCodeError] was reported, caused by [cause].
     */
    fun assertSingleUserCodeError(cause: Throwable) {
        assertEquals(1, errors.size, "errors: $errors")
        val error = assertIs<SdkError.UserCodeError>(errors.single())
        assertSame(cause, error.cause)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }
}
