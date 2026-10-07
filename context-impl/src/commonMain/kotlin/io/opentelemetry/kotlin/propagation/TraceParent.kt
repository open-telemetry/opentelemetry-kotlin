package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.propagation.utils.SPAN_ID_HEX_LENGTH
import io.opentelemetry.kotlin.propagation.utils.TRACE_FLAG_RANDOM
import io.opentelemetry.kotlin.propagation.utils.TRACE_FLAG_SAMPLED
import io.opentelemetry.kotlin.propagation.utils.TRACE_ID_HEX_LENGTH
import io.opentelemetry.kotlin.propagation.utils.decodeTraceFlagsOrZero
import io.opentelemetry.kotlin.propagation.utils.encodeTraceFlags
import io.opentelemetry.kotlin.propagation.utils.isValidLowercaseHex

/**
 * Implementation of a W3C `traceparent` header.
 *
 * https://www.w3.org/TR/trace-context/#traceparent-header
 */
public class TraceParent private constructor(
    val version: String,
    val traceId: String,
    val spanId: String,
    val isSampled: Boolean,
    val isRandom: Boolean,
) {

    fun encode(): String = buildString {
        append(version)
        append(FIELD_SEPARATOR)
        append(traceId)
        append(FIELD_SEPARATOR)
        append(spanId)
        append(FIELD_SEPARATOR)
        append(encodeTraceFlags(isSampled, isRandom))
    }

    companion object {
        const val VERSION_00: String = "00"

        private const val FORBIDDEN_VERSION = "ff"
        private const val VERSION_LEN = 2
        private const val FLAGS_LEN = 2
        private const val LEN_V00 = 55
        private const val EXPECTED_FIELD_COUNT = 4
        private const val FIELD_SEPARATOR = '-'

        /**
         * Create a [TraceParent] if the given inputs are valid.
         * Returns null if there is at least one invalid parameter.
         */
        fun create(
            version: String,
            traceId: String,
            spanId: String,
            isSampled: Boolean,
            isRandom: Boolean,
        ): TraceParent? {
            val valid = version.length == VERSION_LEN && version.isValidLowercaseHex() && version != FORBIDDEN_VERSION &&
                traceId.length == TRACE_ID_HEX_LENGTH && traceId.isValidLowercaseHex() &&
                spanId.length == SPAN_ID_HEX_LENGTH && spanId.isValidLowercaseHex()
            return if (valid) {
                TraceParent(version, traceId, spanId, isSampled, isRandom)
            } else {
                null
            }
        }

        fun decode(header: String): TraceParent? {
            if (header.length < LEN_V00) {
                return null
            }
            if (header.any { it.isUpperCase() }) {
                return null
            }

            val parts = header.split(FIELD_SEPARATOR)
            if (parts.size < EXPECTED_FIELD_COUNT) {
                return null
            }

            val version = parts[0]
            if (version == VERSION_00 && (parts.size != EXPECTED_FIELD_COUNT || header.length != LEN_V00)) {
                // strict for version 00: exactly 4 fields and exactly 55 chars.
                return null
            }

            val flagsStr = parts[3]
            if (flagsStr.length != FLAGS_LEN || !flagsStr.isValidLowercaseHex()) {
                return null
            }

            val flags = flagsStr.decodeTraceFlagsOrZero()
            return create(
                version = version,
                traceId = parts[1],
                spanId = parts[2],
                isSampled = (flags and TRACE_FLAG_SAMPLED) != 0,
                isRandom = (flags and TRACE_FLAG_RANDOM) != 0,
            )
        }
    }
}
