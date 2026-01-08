package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Contains details about the trace.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#spancontext
 */
@ThreadSafe
@ExperimentalApi
public interface TraceFlags {

    /**
     * True if the trace is sampled.
     */
    @ThreadSafe
    public val isSampled: Boolean

    /**
     * True if the trace is random.
     */
    @ThreadSafe
    public val isRandom: Boolean

    /**
     * Returns the hexadecimal representation of the trace flags as a 2-character lowercase string.
     *
     * Possible values:
     * - "00" = no flags set (neither sampled nor random)
     * - "01" = sampled only (0b000000001)
     * - "02" = random only (0b000000010)
     * - "03" = both sampled and random (0b000000011)
     */
    @ThreadSafe
    public val hex: String
}
