package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.ContextKeyImpl
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.factory.isAllZerosHex
import io.opentelemetry.kotlin.factory.isValidHex
import io.opentelemetry.kotlin.init.B3Format
import io.opentelemetry.kotlin.platformLog
import io.opentelemetry.kotlin.tracing.SpanContext

/**
 * B3 trace context propagator supporting both single-header and multi-header formats.
 *
 * Extract always attempts single-header first, falling back to multi-header.
 * Inject format is controlled by [format].
 *
 * https://github.com/openzipkin/b3-propagation
 */
@OptIn(ExperimentalApi::class)
internal class B3Propagator(
    private val format: B3Format,
    private val traceFlagsFactory: TraceFlagsFactory,
    private val traceStateFactory: TraceStateFactory,
    private val spanContextFactory: SpanContextFactory,
    private val spanFactory: SpanFactory,
) : TextMapPropagator {

    override fun fields(): Collection<String> = when (format) {
        B3Format.SINGLE -> SINGLE_FIELDS
        B3Format.MULTI -> MULTI_FIELDS
    }

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        val spanContext = context.extractSpan().spanContext
        if (!spanContext.isValid) { return }
        val debug = context.isB3Debug()
        when (format) {
            B3Format.SINGLE -> injectSingle(spanContext, debug, carrier, setter)
            B3Format.MULTI -> injectMulti(spanContext, debug, carrier, setter)
        }
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context =
        extractSingle(context, carrier, getter)
            ?: extractMulti(context, carrier, getter)
            ?: context

    private fun <T> injectSingle(
        spanContext: SpanContext,
        debug: Boolean,
        carrier: T,
        setter: TextMapSetter<T>,
    ) {
        val flag = when {
            debug -> FLAGS.DEBUG
            spanContext.traceFlags.isSampled -> FLAGS.SAMPLED
            else -> FLAGS.DEFAULT
        }
        val value = buildString(SINGLE_HEADER_SIZE) {
            append(spanContext.traceId)
            append('-')
            append(spanContext.spanId)
            append('-')
            append(flag)
        }
        setter.set(carrier, COMBINED_HEADER, value)
    }

    private fun <T> injectMulti(
        spanContext: SpanContext,
        debug: Boolean,
        carrier: T,
        setter: TextMapSetter<T>,
    ) {
        setter.set(carrier, TRACE_ID_HEADER, spanContext.traceId)
        setter.set(carrier, SPAN_ID_HEADER, spanContext.spanId)
        if (debug) {
            setter.set(carrier, DEBUG_HEADER, "1")
            setter.set(carrier, SAMPLED_HEADER, "1")
        } else {
            val sampledValue = if (spanContext.traceFlags.isSampled) { "1" } else { "0" }
            setter.set(carrier, SAMPLED_HEADER, sampledValue)
        }
    }

    private fun <T> extractSingle(context: Context, carrier: T, getter: TextMapGetter<T>): Context? {
        val header = getter.get(carrier, COMBINED_HEADER) ?: return null
        val parts = header.split(DELIMITER)
        if (parts.size !in 2..4) {
            platformLog("B3 single header has wrong number of parts: $header")
            return null
        }
        val traceId = normalizeTraceId(parts[0]) ?: run {
            platformLog("B3 invalid traceId in single header: ${parts[0]}")
            return null
        }
        val rawSpanId = parts[1]
        val sampled = parts.getOrNull(2)
        val debug = sampled == "d"
        val spanContext = buildContext(debug, sampled, traceId, rawSpanId)
        if (!spanContext.isValid) {
            platformLog("B3 invalid spanId in single header: $rawSpanId")
            return null
        }
        return context.storeSpan(spanFactory.fromSpanContext(spanContext)).let {
            if (debug) { it.withB3Debug() } else { it }
        }
    }

    private fun <T> extractMulti(context: Context, carrier: T, getter: TextMapGetter<T>): Context? {
        val rawTraceId = getter.get(carrier, TRACE_ID_HEADER)
        val rawSpanId = getter.get(carrier, SPAN_ID_HEADER) ?: return null
        val traceId = normalizeTraceId(rawTraceId) ?: run {
            if (rawTraceId != null) { platformLog("B3 invalid traceId in multi header: $rawTraceId") }
            return null
        }
        val debug = getter.get(carrier, DEBUG_HEADER) == "1"
        val sampled = getter.get(carrier, SAMPLED_HEADER)
        val spanContext = buildContext(debug, sampled, traceId, rawSpanId)
        if (!spanContext.isValid) {
            platformLog("B3 invalid spanId in multi header: $rawSpanId")
            return null
        }
        return context.storeSpan(spanFactory.fromSpanContext(spanContext)).let {
            if (debug) { it.withB3Debug() } else { it }
        }
    }

    private fun buildContext(
        debug: Boolean,
        sampled: String?,
        traceId: String,
        rawSpanId: String
    ): SpanContext {
        val traceFlags = if (debug || isSampledValue(sampled)) {
            traceFlagsFactory.fromHex("01")
        } else {
            traceFlagsFactory.fromHex("00")
        }
        return spanContextFactory.create(
            traceId = traceId,
            spanId = rawSpanId,
            traceFlags = traceFlags,
            traceState = traceStateFactory.default,
            isRemote = true,
        )
    }

    private fun normalizeTraceId(raw: String?): String? {
        if (raw == null) { return null }
        return when (raw.length) {
            TRACE_ID_LENGTH -> raw.takeIf { it.isValidHex() && !it.isAllZerosHex() }
            TRACE_ID_LENGTH / 2 -> raw.padStart(TRACE_ID_LENGTH, '0')
                .takeIf { it.isValidHex() && !it.isAllZerosHex() }
            else -> null
        }
    }

    private fun isSampledValue(value: String?): Boolean =
        value == "1" || value?.lowercase() == "true"

    private fun Context.withB3Debug(): Context = set(DEBUG_CONTEXT_KEY, true)
    private fun Context.isB3Debug(): Boolean = get(DEBUG_CONTEXT_KEY) == true

    companion object {
        internal val DEBUG_CONTEXT_KEY: ContextKey<Boolean> = ContextKeyImpl("b3-debug")

        private const val TRACE_ID_HEADER = "X-B3-TraceId"
        private const val SPAN_ID_HEADER = "X-B3-SpanId"
        private const val SAMPLED_HEADER = "X-B3-Sampled"
        private const val DEBUG_HEADER = "X-B3-Flags"
        private const val COMBINED_HEADER = "b3"
        private const val DELIMITER = "-"
        private const val TRACE_ID_LENGTH = 32
        private const val SINGLE_HEADER_SIZE = 51 // 32 + 1 + 16 + 1 + 1

        private val SINGLE_FIELDS = listOf(COMBINED_HEADER)
        private val MULTI_FIELDS = listOf(TRACE_ID_HEADER, SPAN_ID_HEADER, SAMPLED_HEADER)

        private object FLAGS {
            const val DEBUG = 'd'
            const val SAMPLED = '1'
            const val DEFAULT = '0'
        }
    }
}
