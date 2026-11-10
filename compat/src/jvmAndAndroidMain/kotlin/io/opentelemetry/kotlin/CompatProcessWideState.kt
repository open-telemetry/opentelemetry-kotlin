package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.OtelJavaCurrentContextStorage
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.internalInstallContextStorage
import io.opentelemetry.kotlin.context.internalRootContext
import io.opentelemetry.kotlin.context.internalUninstallContextStorage
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.setProcessSdkErrorHandler

/**
 * The process-wide state that a compat SDK claimed at init and must release at shutdown.
 */
internal class CompatProcessWideState(
    private val installedStorage: CurrentContextStorage?,
    private val ownsErrorSink: Boolean,
) {

    @OptIn(TempContextStandInApi::class)
    fun release() {
        installedStorage?.let(::internalUninstallContextStorage)
        if (ownsErrorSink) {
            setProcessSdkErrorHandler(null)
        }
    }
}

/**
 * Installs the opentelemetry-java bridge as the process-wide context storage, so that Kotlin and
 * Java share one current context, and claims the process-wide error sink for [errorHandler].
 *
 * If another storage is already installed it's used instead, and a warning is reported because
 * the Kotlin current context may then not be shared with opentelemetry-java. [api] names the
 * entrypoint in that warning.
 */
@OptIn(ExperimentalApi::class, TempContextStandInApi::class)
internal fun claimCompatProcessWideState(api: String, errorHandler: SdkErrorHandler): CompatProcessWideState {
    val storage = OtelJavaCurrentContextStorage(::internalRootContext)
    val installed = internalInstallContextStorage(storage)
    if (!installed) {
        errorHandler.onError(
            SdkError.ApiMisuse(
                api = api,
                message = "A CurrentContextStorage is already installed for this process, so it's used instead " +
                    "of opentelemetry-java's ContextStorage. The Kotlin and Java current contexts may not be shared.",
                severity = SdkErrorSeverity.WARNING,
            )
        )
    }
    return CompatProcessWideState(
        installedStorage = storage.takeIf { installed },
        ownsErrorSink = setProcessSdkErrorHandler(errorHandler),
    )
}
