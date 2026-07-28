package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.factory.isValidLowercaseHex
import kotlin.jvm.JvmInline

@JvmInline
internal value class Threshold(internal val value: Long) {

    init {
        require(value in 0..<MAX) { "threshold must be between 0 and 2^56, got $value" }
    }

    operator fun compareTo(other: Threshold): Int {
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
        internal const val MAX: Long = 1L shl 56

        fun decode(encoded: String): Threshold? {
            return encoded.takeIf { it.isNotBlank() && it.length <= 14 && it.isValidLowercaseHex() }
                ?.padEnd(14, '0')
                ?.toLong(16)
                ?.let(::Threshold)
        }

        fun fromRatio(ratio: Double): Threshold {
            return Threshold(MAX - (ratio * MAX).toLong())
        }
    }
}
