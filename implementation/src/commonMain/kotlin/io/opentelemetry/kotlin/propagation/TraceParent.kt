package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.platformLog
import io.opentelemetry.kotlin.tracing.TraceFlags

/**
 * Implementation of a W3C `traceparent` header.
 *
 * https://www.w3.org/TR/trace-context/#traceparent-header
 */
@OptIn(ExperimentalApi::class)
internal class TraceParent private constructor(
    val version: String,
    val traceId: String,
    val spanId: String,
    val traceFlags: TraceFlags,
) {

    fun encode(): String = buildString {
        append(version)
        append(FIELD_SEPARATOR)
        append(traceId)
        append(FIELD_SEPARATOR)
        append(spanId)
        append(FIELD_SEPARATOR)
        append(encodeFlags(traceFlags))
    }

    companion object {
        const val VERSION_00: String = "00"

        private const val FORBIDDEN_VERSION = "ff"
        private const val VERSION_LEN = 2
        private const val TRACE_ID_LEN = 32
        private const val SPAN_ID_LEN = 16
        private const val FLAGS_LEN = 2
        private const val LEN_V00 = 55
        private const val EXPECTED_FIELD_COUNT = 4
        private const val FIELD_SEPARATOR = '-'
        private const val FLAG_SAMPLED = 0b0000_0001
        private const val FLAG_RANDOM = 0b0000_0010
        private const val HEX_RADIX = 16

        /**
         * Create a [TraceParent] if the given inputs are valid.
         * Returns null if there is at least one invalid parameter.
         */
        fun create(
            version: String,
            traceId: String,
            spanId: String,
            traceFlags: TraceFlags,
        ): TraceParent? {
            val errorMessage =
                if (version.length != VERSION_LEN || !version.isLowerHex() || version == FORBIDDEN_VERSION) {
                    "version must be $VERSION_LEN lowercase hex characters and not $FORBIDDEN_VERSION"
                } else if (traceId.length != TRACE_ID_LEN || !traceId.isLowerHex()) {
                    "traceId must be $TRACE_ID_LEN lowercase hex characters"
                } else if (spanId.length != SPAN_ID_LEN || !spanId.isLowerHex()) {
                    "spanId must be $SPAN_ID_LEN lowercase hex characters"
                } else {
                    null
                }

            return if (errorMessage == null) {
                TraceParent(version, traceId, spanId, traceFlags)
            } else {
                platformLog(errorMessage)
                null
            }
        }

        fun decode(header: String, traceFlagsFactory: TraceFlagsFactory): TraceParent? {
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
            if (flagsStr.length != FLAGS_LEN || !flagsStr.isLowerHex()) {
                return null
            }

            return create(
                version = version,
                traceId = parts[1],
                spanId = parts[2],
                traceFlags = traceFlagsFactory.fromHex(flagsStr),
            )
        }

        private fun encodeFlags(flags: TraceFlags): String {
            var byte = 0
            if (flags.isSampled) {
                byte = byte or FLAG_SAMPLED
            }
            if (flags.isRandom) {
                byte = byte or FLAG_RANDOM
            }
            return byte.toString(HEX_RADIX).padStart(FLAGS_LEN, '0')
        }

        private fun String.isLowerHex(): Boolean = all { it in '0'..'9' || it in 'a'..'f' }
    }
}
