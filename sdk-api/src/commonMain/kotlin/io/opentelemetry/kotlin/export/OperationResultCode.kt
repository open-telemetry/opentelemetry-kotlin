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

/**
 * Combines two [OperationResultCode] values. Returns [Success] only if both are [Success].
 */
@ExperimentalApi
public infix fun OperationResultCode.and(other: OperationResultCode): OperationResultCode = when {
    this == OperationResultCode.Success && other == OperationResultCode.Success -> OperationResultCode.Success
    else -> OperationResultCode.Failure
}
