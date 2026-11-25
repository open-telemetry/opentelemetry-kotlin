package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Whether an operation was successful or not.
 *
 * `Failure` results may convey what went wrong via a subtype, e.g. [Timeout] when the operation was
 * cut short by a timeout. `is` checks against [Failure] match those subtypes.
 */
@ExperimentalApi
public sealed class OperationResultCode {

    /**
     * Indicates that the operation was successful.
     */
    public object Success : OperationResultCode()

    /**
     * Indicates that the operation failed.
     *
     * `Failure` itself stands for a generic failure. Subtypes such as [Timeout] convey the kind
     * of failure.
     */
    public open class Failure : OperationResultCode() {

        /**
         * The [Failure] result returned for a generic failure; the same instance every time.
         */
        public companion object : Failure() {
            override fun toString(): String = "Failure"
        }
    }

    /**
     * Indicates that the operation failed because its timeout elapsed before it completed.
     */
    public object Timeout : Failure()
}
