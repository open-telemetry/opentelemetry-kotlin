package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefaultSuspend

/**
 * Runs exporter code, reporting anything it throws and returning [OperationResultCode.Failure] instead.
 */
internal suspend fun SdkErrorHandler.guardExporterCode(
    details: String,
    action: suspend () -> OperationResultCode,
): OperationResultCode = guardOrDefaultSuspend(OperationResultCode.Failure, details, action)
