package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.AtomicBoolean
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity

private const val DETACH_API = "Scope.detach"
private const val ALREADY_DETACHED_MSG = "Scope.detach() called on an already-detached scope"
private const val OUT_OF_ORDER_MSG = "Scope.detach() called out of order — context has already changed"

/**
 * Wraps the [Scope] returned by a storage and reports misuse of it.
 */
@OptIn(ExperimentalApi::class)
internal class MisuseReportingScope(
    private val delegate: Scope,
    private val attached: Context,
    private val current: () -> Context,
    private val report: (SdkError) -> Unit,
) : Scope {

    private val detached = AtomicBoolean(false)

    override fun detach(): Boolean {
        if (!detached.compareAndSet(expect = false, update = true)) {
            reportMisuse(ALREADY_DETACHED_MSG)
            return false
        }
        if (current() !== attached) {
            reportMisuse(OUT_OF_ORDER_MSG)
        }
        return try {
            delegate.detach()
        } catch (e: Throwable) {
            report(SdkError.UserCodeError(e, "Scope.detach threw", SdkErrorSeverity.WARNING))
            false
        }
    }

    private fun reportMisuse(message: String) {
        report(SdkError.ApiMisuse(api = DETACH_API, message = message, severity = SdkErrorSeverity.WARNING))
    }
}
