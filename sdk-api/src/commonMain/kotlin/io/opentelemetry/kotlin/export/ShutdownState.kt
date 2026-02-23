package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Execute code or return defaults depending on whether this is shutdown or not, respectively.
 */
@ExperimentalApi
public abstract class ShutdownState {

    /**
     * Whether this is shutdown or not
     */
    public abstract val isShutdown: Boolean

    /**
     * Run the given [action] if this isn't shutdown. Otherwise, return [default].
     * This method will not handle exceptions thrown by [action].
     */
    public inline fun <T> ifActiveOrElse(default: T, action: () -> T): T =
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
    public inline fun ifActive(
        action: () -> OperationResultCode,
    ): OperationResultCode =
        ifActiveOrElse(OperationResultCode.Failure, action)

    /**
     * Run the given [action] if this isn't shutdown. Do nothing otherwise.
     * This method will not handle exceptions thrown by [action].
     */
    public inline fun execute(action: () -> Unit) {
        ifActiveOrElse(Unit, action)
    }
}
