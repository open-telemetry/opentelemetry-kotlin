package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

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
@ExperimentalApi
public val TraceFlags.hex: String
    get() = when {
        isRandom && isSampled -> "03"
        isRandom && !isSampled -> "02"
        !isRandom && isSampled -> "01"
        else -> "00"
    }
