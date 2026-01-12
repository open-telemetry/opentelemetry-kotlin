package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Represents the status of an operation.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#set-status
 */
@ThreadSafe
@ExperimentalApi
public enum class StatusCode {

    /**
     * Default status.
     */
    UNSET,

    /**
     * The operation completed successfully.
     */
    OK,

    /**
     * The operation completed with an error.
     */
    ERROR
}
