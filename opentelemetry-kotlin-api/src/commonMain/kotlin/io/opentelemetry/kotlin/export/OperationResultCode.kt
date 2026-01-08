package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Whether an operation was successful or not.
 */
@ExperimentalApi
public sealed class OperationResultCode {

    /**
     * Indicates that the operation was successful.
     */
    public object Success : OperationResultCode()

    /**
     * Indicates that the operation failed.
     */
    public object Failure : OperationResultCode()
}
