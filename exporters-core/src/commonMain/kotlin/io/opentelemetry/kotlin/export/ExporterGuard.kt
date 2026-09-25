package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefaultSuspend
import io.opentelemetry.kotlin.error.reportUserCodeError
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs exporter code, reporting anything it throws and returning [OperationResultCode.Failure] instead.
 */
internal suspend fun SdkErrorHandler.guardExporterCode(
    details: String,
    action: suspend () -> OperationResultCode,
): OperationResultCode = try {
    guardOrDefaultSuspend(OperationResultCode.Failure, details, action)
} catch (exc: CancellationException) {
    currentCoroutineContext().ensureActive()
    reportUserCodeError(exc, details)
    OperationResultCode.Failure
}
