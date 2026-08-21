package io.opentelemetry.kotlin.factory

/**
 * The number of bytes in a trace ID.
 */
internal const val TRACE_ID_BYTES: Int = 16

/**
 * The number of bytes in a span ID.
 */
internal const val SPAN_ID_BYTES: Int = 8

/**
 * The number of characters in a hex-encoded trace ID.
 */
internal const val TRACE_ID_HEX_LENGTH: Int = TRACE_ID_BYTES * 2

/**
 * The number of characters in a hex-encoded span ID.
 */
internal const val SPAN_ID_HEX_LENGTH: Int = SPAN_ID_BYTES * 2

/**
 * Returns true if the bytes are a valid trace ID, i.e. 16 bytes with at least one non-zero byte.
 */
internal fun ByteArray.isValidTraceIdBytes(): Boolean = size == TRACE_ID_BYTES && !isAllZeroBytes()

/**
 * Returns true if the bytes are a valid span ID, i.e. 8 bytes with at least one non-zero byte.
 */
internal fun ByteArray.isValidSpanIdBytes(): Boolean = size == SPAN_ID_BYTES && !isAllZeroBytes()

/**
 * Returns true if every byte is zero. An empty [ByteArray] is considered all zeros.
 */
internal fun ByteArray.isAllZeroBytes(): Boolean = all { it == 0.toByte() }
