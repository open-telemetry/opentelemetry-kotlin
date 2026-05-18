package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.factory.isValidLowercaseHex

private const val MAX_THRESHOLD: Long = 1L shl 56

internal class Threshold(val value: Long) : Comparable<Threshold> {

    init {
        require(value in 0..<MAX_THRESHOLD) { "threshold must be between 0 and 2^56, got $value" }
    }

    override fun compareTo(other: Threshold): Int {
        return value.compareTo(other.value)
    }

    fun encode(): String {
        return if (value == 0L) {
            "0"
        } else {
            value.toString(16).padStart(14, '0').trimEnd('0')
        }
    }

    companion object {
        fun decode(encoded: String): Threshold? {
            return encoded.takeIf { it.isNotBlank() && it.length <= 14 && it.isValidLowercaseHex() }
                ?.padEnd(14, '0')
                ?.toLong(16)
                ?.let(::Threshold)
        }
    }
}