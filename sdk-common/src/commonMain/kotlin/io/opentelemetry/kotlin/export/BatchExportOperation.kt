package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.error.guardOrDefaultSuspend

/**
 * Performs an export operation on each element in a List and returns a success code if each
 * operation is successful, or a failure code if any operation fails.
 */
public fun <T> batchExportOperation(
    elements: List<T>,
    sdkErrorHandler: SdkErrorHandler,
    action: (T) -> OperationResultCode
): OperationResultCode {
    var success = true

    elements.forEach {
        val exportResult = sdkErrorHandler.guardOrDefault(
            OperationResultCode.Failure,
            "Export operation failed",
        ) {
            action(it)
        }
        success = success && exportResult == OperationResultCode.Success
    }
    return when {
        success -> OperationResultCode.Success
        else -> OperationResultCode.Failure
    }
}

/**
 * Performs an export operation on each element in a List and returns a success code if each
 * operation is successful, or a failure code if any operation fails.
 */
public suspend fun <T> batchExportOperationSuspend(
    elements: List<T>,
    sdkErrorHandler: SdkErrorHandler,
    action: suspend (T) -> OperationResultCode
): OperationResultCode {
    var success = true

    elements.forEach {
        val exportResult = sdkErrorHandler.guardOrDefaultSuspend(
            OperationResultCode.Failure,
            "Export operation failed",
        ) {
            action(it)
        }
        success = success && exportResult == OperationResultCode.Success
    }
    return when {
        success -> OperationResultCode.Success
        else -> OperationResultCode.Failure
    }
}
