package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ReportOnce
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError

private const val LATER_FAILURES_MSG = "Later failures are not reported."

/**
 * Wraps a [CurrentContextStorage] so that misuse of it is reported and so that nothing the
 * storage throws escapes. Each kind of failure or misuse is reported the first time only, because
 * context operations run on hot paths.
 */
@OptIn(ExperimentalApi::class)
internal class MisuseReportingCurrentContextStorage(
    private val delegate: CurrentContextStorage,
    private val root: () -> Context,
    private val report: (SdkError) -> Unit = ::reportError,
) : CurrentContextStorage {

    private val currentFailure = ReportOnce()
    private val attachFailure = ReportOnce()
    private val detachFailure = ReportOnce()
    private val doubleDetachMisuse = ReportOnce()
    private val outOfOrderMisuse = ReportOnce()

    override fun current(): Context = try {
        delegate.current()
    } catch (e: Throwable) {
        reportUserCodeError(currentFailure, e, "CurrentContextStorage.current threw. $LATER_FAILURES_MSG")
        root()
    }

    override fun attach(context: Context): Scope {
        val scope = try {
            delegate.attach(context)
        } catch (e: Throwable) {
            reportUserCodeError(attachFailure, e, "CurrentContextStorage.attach threw. $LATER_FAILURES_MSG")
            return FallbackScope
        }
        return MisuseReportingScope(
            delegate = scope,
            attached = current(),
            current = ::current,
            report = report,
            detachFailure = detachFailure,
            doubleDetachMisuse = doubleDetachMisuse,
            outOfOrderMisuse = outOfOrderMisuse,
        )
    }

    private fun reportUserCodeError(failure: ReportOnce, e: Throwable, message: String) {
        if (failure.tryAcquire()) {
            report(SdkError.UserCodeError(e, message, SdkErrorSeverity.WARNING))
        }
    }
}
