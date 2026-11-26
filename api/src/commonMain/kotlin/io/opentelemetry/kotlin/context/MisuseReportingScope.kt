package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.AtomicBoolean
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ReportOnce
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity

private const val DETACH_API = "Scope.detach"
private const val ALREADY_DETACHED_MSG =
    "Scope.detach() called on an already-detached scope. Later occurrences are not reported."
private const val OUT_OF_ORDER_MSG =
    "Scope.detach() called out of order — context has already changed. Later occurrences are not reported."
private const val DETACH_THREW_MSG = "Scope.detach threw. Later failures are not reported."

/**
 * Wraps the [Scope] returned by a storage and reports misuse of it. The latches are shared by every
 * scope of one storage, so each kind of misuse is reported once per storage.
 */
@OptIn(ExperimentalApi::class)
internal class MisuseReportingScope(
    private val delegate: Scope,
    private val attached: Context,
    private val current: () -> Context,
    private val report: (SdkError) -> Unit,
    private val detachFailure: ReportOnce,
    private val doubleDetachMisuse: ReportOnce,
    private val outOfOrderMisuse: ReportOnce,
) : Scope {

    private val detached = AtomicBoolean(false)

    override fun detach(): Boolean {
        if (!detached.compareAndSet(expect = false, update = true)) {
            reportMisuse(doubleDetachMisuse, ALREADY_DETACHED_MSG)
            return false
        }
        if (current() !== attached) {
            reportMisuse(outOfOrderMisuse, OUT_OF_ORDER_MSG)
        }
        return try {
            delegate.detach()
        } catch (e: Throwable) {
            if (detachFailure.tryAcquire()) {
                report(SdkError.UserCodeError(e, DETACH_THREW_MSG, SdkErrorSeverity.WARNING))
            }
            false
        }
    }

    private fun reportMisuse(latch: ReportOnce, message: String) {
        if (latch.tryAcquire()) {
            report(SdkError.ApiMisuse(api = DETACH_API, message = message, severity = SdkErrorSeverity.WARNING))
        }
    }
}
