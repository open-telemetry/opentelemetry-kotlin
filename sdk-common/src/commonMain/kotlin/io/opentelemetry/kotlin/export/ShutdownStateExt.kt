package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Run the given [action] if this isn't shutdown. Otherwise, return [default].
 * This method will not handle exceptions thrown by [action].
 */
@ExperimentalApi
public inline fun <T> ShutdownState.ifActiveOrElse(default: T, action: () -> T): T =
    if (isShutdown) {
        default
    } else {
        action()
    }

/**
 * Run the given [action] and return the resulting [OperationResultCode] if this isn't shutdown.
 * Otherwise, return [OperationResultCode.Failure].
 * This method will not handle exceptions thrown by [action].
 */
@ExperimentalApi
public inline fun ShutdownState.ifActive(
    action: () -> OperationResultCode,
): OperationResultCode =
    ifActiveOrElse(OperationResultCode.Failure, action)

/**
 * Run the given [action] if this isn't shutdown. Do nothing otherwise.
 * This method will not handle exceptions thrown by [action].
 */
@ExperimentalApi
public inline fun ShutdownState.execute(action: () -> Unit) {
    ifActiveOrElse(Unit, action)
}
