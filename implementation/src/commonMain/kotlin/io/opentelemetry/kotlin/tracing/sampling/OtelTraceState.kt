package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.factory.isValidLowercaseHex

internal class OtelTraceState private constructor(
    private val pairs: LinkedHashMap<String, String>
) {
    val rv: Long?
        get() = pairs["rv"]?.randomness()

    val th: Threshold?
        get() = pairs["th"]?.let { Threshold.decode(it) }

    fun applyThreshold(threshold: Threshold) {
        val current = th
        if(current == null || threshold > current) {
            setThreshold(threshold)
        }
    }

    fun setThreshold(threshold: Threshold) {
        pairs["th"] = threshold.encode()
    }

    fun eraseThreshold() {
        pairs.remove("th")
    }

    fun encode(): String = pairs.entries.joinToString(";") { (key, value) -> "$key:$value" }

    private fun String.randomness(): Long? =
        takeIf { length == 14 && isValidLowercaseHex() }
            ?.toLong(16)

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
}
