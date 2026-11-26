package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.jvm.JvmField

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

            /**
             * Keeps JVM binaries compiled against the former `object Failure` working: they read
             * this static field. Hidden from Kotlin sources, which use [Failure] itself.
             */
            @JvmField
            @Deprecated(
                "Binary compatibility only. Use OperationResultCode.Failure instead.",
                level = DeprecationLevel.HIDDEN,
            )
            public val INSTANCE: Failure = this
        }
    }

    /**
     * Indicates that the operation failed because its timeout elapsed before it completed.
     */
    public object Timeout : Failure()
}
