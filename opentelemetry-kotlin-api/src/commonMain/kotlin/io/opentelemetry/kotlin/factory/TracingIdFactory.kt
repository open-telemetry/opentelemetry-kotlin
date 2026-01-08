package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
public interface TracingIdFactory {

    /**
     * Generates a new ID for a span.
     */
    public fun generateSpanIdBytes(): ByteArray

    /**
     * Generates a new ID for a trace.
     */
    public fun generateTraceIdBytes(): ByteArray

    /**
     * An invalid trace ID.
     */
    public val invalidTraceId: ByteArray

    /**
     * An invalid span ID.
     */
    public val invalidSpanId: ByteArray
}

/**
 * Encodes Span/Trace ID bytes as a hex string.
 */
@ExperimentalApi
public fun ByteArray.toHexString(): String {
    val result = StringBuilder(size * 2)
    for (b in this) {
        val i = b.toInt() and 0xFF
        result.append(i.toString(16).padStart(2, '0'))
    }
    return result.toString()
}

/**
 * Encodes Span/Trace ID hex string as a ByteArray.
 */
@ExperimentalApi
public fun String.hexToByteArray(): ByteArray {
    require(length % 2 == 0)
    val out = ByteArray(length / 2)
    for (i in out.indices) {
        val hi = this[i * 2].digitToInt(16)
        val lo = this[i * 2 + 1].digitToInt(16)
        out[i] = ((hi shl 4) or lo).toByte()
    }
    return out
}
