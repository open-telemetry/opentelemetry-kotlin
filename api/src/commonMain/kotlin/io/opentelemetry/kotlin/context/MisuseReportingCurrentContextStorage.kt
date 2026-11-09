package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError

/**
 * Wraps a [CurrentContextStorage] so that misuse of it is reported and so that nothing the
 * storage throws escapes.
 */
@OptIn(ExperimentalApi::class)
internal class MisuseReportingCurrentContextStorage(
    private val delegate: CurrentContextStorage,
    private val root: () -> Context,
    private val report: (SdkError) -> Unit = ::reportError,
) : CurrentContextStorage {

    override fun current(): Context = try {
        delegate.current()
    } catch (e: Throwable) {
        reportUserCodeError(e, "CurrentContextStorage.current threw")
        root()
    }

    override fun attach(context: Context): Scope {
        if (current() === context) {
            report(
                SdkError.ApiMisuse(
                    api = "Context.attach",
                    message = "Context.attach() called with the context that is already current",
                    severity = SdkErrorSeverity.WARNING,
                )
            )
        }
        val scope = try {
            delegate.attach(context)
        } catch (e: Throwable) {
            reportUserCodeError(e, "CurrentContextStorage.attach threw")
            return FallbackScope
        }
        return MisuseReportingScope(
            delegate = scope,
            attached = current(),
            current = ::current,
            report = report,
        )
    }

    private fun reportUserCodeError(e: Throwable, message: String) {
        report(SdkError.UserCodeError(e, message, SdkErrorSeverity.WARNING))
    }
}
