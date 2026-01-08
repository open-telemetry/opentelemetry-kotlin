package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity

/**
 * Performs an export operation on each element in a List and returns a success code if each
 * operation is successful, or a failure code if any operation fails.
 */
@OptIn(ExperimentalApi::class)
internal fun <T> batchExportOperation(
    elements: List<T>,
    sdkErrorHandler: SdkErrorHandler,
    action: (T) -> OperationResultCode
): OperationResultCode {
    var success = true

    elements.forEach {
        try {
            val exportResult = action(it)
            success = success && exportResult == OperationResultCode.Success
        } catch (exc: Throwable) {
            success = false
            sdkErrorHandler.onUserCodeError(
                exc,
                "Export operation failed",
                SdkErrorSeverity.WARNING
            )
        }
    }
    return when {
        success -> OperationResultCode.Success
        else -> OperationResultCode.Failure
    }
}
