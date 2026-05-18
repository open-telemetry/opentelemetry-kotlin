package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.factory.isValidLowercaseHex

internal data class Randomness(private val value: Long) {
    operator fun compareTo(threshold: Threshold): Int {
        return this.value.compareTo(threshold.value)
    }

    companion object {
        fun decode(encoded: String): Randomness? {
            return encoded.takeIf { it.length == 14 && it.isValidLowercaseHex() }
                ?.toLong(16)
                ?.let(::Randomness)
        }

        fun fromTraceId(traceId: String): Randomness {
            return Randomness(
                (byteFromBase16(traceId[18], traceId[19]) shl 48) or
                    (byteFromBase16(traceId[20], traceId[21]) shl 40) or
                    (byteFromBase16(traceId[22], traceId[23]) shl 32) or
                    (byteFromBase16(traceId[24], traceId[25]) shl 24) or
                    (byteFromBase16(traceId[26], traceId[27]) shl 16) or
                    (byteFromBase16(traceId[28], traceId[29]) shl 8) or
                    byteFromBase16(traceId[30], traceId[31])
            )
        }

        private fun byteFromBase16(first: Char, second: Char): Long =
            ((first.digitToInt(16) shl 4) or second.digitToInt(16)).toLong()
    }
}
