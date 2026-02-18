package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Execute code or return defaults depending on whether this is shutdown or not, respectively.
 */
@OptIn(ExperimentalApi::class)
public abstract class ShutdownState {
    public abstract val isShutdown: Boolean

    /**
     * Run the given [action] if this isn't shutdown. Otherwise, return [default]
     */
    public inline fun <T> ifActiveOrElse(default: T, action: () -> T): T =
        if (isShutdown) {
            default
        } else {
            action()
        }

    /**
     * Run the given [action] and return the resulting [OperationResultCode] if this isn't shutdown. Otherwise, return [default]
     */
    public inline fun ifActive(
        default: OperationResultCode = OperationResultCode.Failure,
        action: () -> OperationResultCode,
    ): OperationResultCode =
        ifActiveOrElse(default, action)

    /**
     * Run the given [action] if this isn't shutdown. Do nothing otherwise.
     */
    public inline fun execute(action: () -> Unit) {
        ifActiveOrElse(Unit, action)
    }
}
