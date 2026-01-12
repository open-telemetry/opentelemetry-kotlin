package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.tracing.StatusCode

/**
 * Immutable representation of a Status
 */
@ExperimentalApi
public sealed class StatusData(
    public val statusCode: StatusCode,
    public val description: String?
) {

    /**
     * Default status.
     */
    @ThreadSafe
    public object Unset : StatusData(StatusCode.UNSET, null)

    /**
     * The operation completed successfully.
     */
    @ThreadSafe
    public object Ok : StatusData(StatusCode.OK, null)

    /**
     * The operation completed with an error. An optional description of the error may be provided.
     */
    @ThreadSafe
    public class Error(description: String?) : StatusData(StatusCode.ERROR, description)
}
