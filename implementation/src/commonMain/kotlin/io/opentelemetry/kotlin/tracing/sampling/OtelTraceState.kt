package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.factory.isValidLowercaseHex

internal class OtelTraceState private constructor(
    private val pairs: LinkedHashMap<String, String>
) {
    val rv: Long?
        get() = pairs["rv"]?.randomness()

    val th: Long?
        get() = pairs["th"]?.threshold()

    fun setThreshold(threshold: Long) {
        require(threshold in 0x0..0xffffffffffffff)
        pairs["th"] = if (threshold == 0L) {
            "0"
        } else {
            threshold.toString(16).padStart(14, '0').trimEnd('0')
        }
    }

    fun eraseThreshold() {
        pairs.remove("th")
    }

    fun encode(): String = pairs.entries.joinToString(";") { (key, value) -> "$key:$value" }

    companion object {
        fun parse(raw: String?): OtelTraceState {
            val pairs = linkedMapOf<String, String>()
            if (raw.isNullOrBlank()) {
                return OtelTraceState(pairs)
            }
            for (pair in raw.split(';')) {
                val parts = pair.split(':', limit = 2)
                if (parts.size < 2) {
                    continue
                }
                val (key, value) = parts
                if (key !in pairs) {
                    pairs[key] = value
                }
            }
            return OtelTraceState(pairs)
        }
    }

    private fun String.randomness(): Long? =
        takeIf { length == 14 && isValidLowercaseHex() }
            ?.toLong(16)

    private fun String.threshold(): Long? =
        takeIf { isNotBlank() && length <= 14 && isValidLowercaseHex() }
            ?.padEnd(14, '0')
            ?.toLong(16)
}
