package io.opentelemetry.kotlin.propagation.utils

/**
 * The number of bytes in a trace ID.
 */
public const val TRACE_ID_BYTES: Int = 16

/**
 * The number of bytes in a span ID.
 */
public const val SPAN_ID_BYTES: Int = 8

/**
 * The number of characters in a hex-encoded trace ID.
 */
public const val TRACE_ID_HEX_LENGTH: Int = TRACE_ID_BYTES * 2

/**
 * The number of characters in a hex-encoded span ID.
 */
public const val SPAN_ID_HEX_LENGTH: Int = SPAN_ID_BYTES * 2

/**
 * Returns true if the bytes are a valid trace ID, i.e. 16 bytes with at least one non-zero byte.
 */
public fun ByteArray.isValidTraceIdBytes(): Boolean = size == TRACE_ID_BYTES && !isAllZeroBytes()

/**
 * Returns true if the bytes are a valid span ID, i.e. 8 bytes with at least one non-zero byte.
 */
public fun ByteArray.isValidSpanIdBytes(): Boolean = size == SPAN_ID_BYTES && !isAllZeroBytes()

/**
 * Returns true if every byte is zero. An empty [ByteArray] is considered all zeros.
 */
public fun ByteArray.isAllZeroBytes(): Boolean = all { it == 0.toByte() }

/**
 * The trace flags bit that marks a trace as sampled.
 */
public const val TRACE_FLAG_SAMPLED: Int = 0b01

/**
 * The trace flags bit that marks a trace ID as random.
 */
public const val TRACE_FLAG_RANDOM: Int = 0b10

/**
 * Decodes 2 hex chars into trace flags, keeping only the [TRACE_FLAG_SAMPLED] and
 * [TRACE_FLAG_RANDOM] bits. Returns 0 if the input is not 2 hex chars.
 */
public fun String.decodeTraceFlagsOrZero(): Int {
    if (length != 2 || !isValidHex()) {
        return 0
    }
    return toInt(16) and (TRACE_FLAG_SAMPLED or TRACE_FLAG_RANDOM)
}

/**
 * Encodes trace flags as 2 lowercase hex chars.
 */
public fun encodeTraceFlags(isSampled: Boolean, isRandom: Boolean): String = when {
    isSampled && isRandom -> "03"
    isRandom -> "02"
    isSampled -> "01"
    else -> "00"
}
