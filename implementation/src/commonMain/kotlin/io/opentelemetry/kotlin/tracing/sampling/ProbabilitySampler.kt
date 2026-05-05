package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.platformLog
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision
import kotlin.concurrent.Volatile
import kotlin.math.max

internal class ProbabilitySampler(ratio: Double) : Sampler {

    private companion object {
        private const val MAX_THRESHOLD: Long = 1L shl 56

        @Volatile
        private var compatibilityWarningLogged = false
        const val COMPATIBILITY_WARNING = "WARNING: The ProbabilitySampler sampler is presuming TraceIDs are random " +
            "and expects the Trace random flag to be set in confirmation. Please " +
            "upgrade your caller(s) to use W3C Trace Context Level 2."
    }

    init {
        require(ratio in (1.0 / MAX_THRESHOLD)..1.0) { "ratio must be between 2^-56 and 1, got $ratio" }
    }

    private val rejectionThreshold: Long = MAX_THRESHOLD - (ratio * MAX_THRESHOLD).toLong()

    override val description: String = "ProbabilitySampler{$ratio}"

    override fun shouldSample(
        context: Context,
        traceId: String,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>
    ): SamplingResult {
        val parentSpanContext = context.extractSpan().spanContext
        val traceState = parentSpanContext.traceState
        val otelTraceState = OtelTraceState.parse(traceState.get("ot"))

        val explicitRandomness = otelTraceState.rv
        val randomness = if (explicitRandomness != null) {
            explicitRandomness
        } else {
            if (parentSpanContext.isValid && !parentSpanContext.traceFlags.isRandom && !compatibilityWarningLogged) {
                compatibilityWarningLogged = true
                platformLog(COMPATIBILITY_WARNING)
            }
            randomnessFromTraceId(traceId)
        }

        val incomingTh = otelTraceState.th
        if (
            incomingTh != null &&
            parentSpanContext.isValid &&
            (randomness >= incomingTh) != parentSpanContext.traceFlags.isSampled
        ) {
            otelTraceState.eraseThreshold()
        }

        otelTraceState.setThreshold(max(otelTraceState.th ?: 0L, rejectionThreshold))

        val decision = if (randomness >= rejectionThreshold) {
            Decision.RECORD_AND_SAMPLE
        } else {
            Decision.DROP
        }

        return SamplingResultImpl(
            decision = decision,
            attributes = AttributesModel(),
            traceState = traceState.put("ot", otelTraceState.encode()),
        )
    }

    private fun randomnessFromTraceId(traceId: String): Long =
        (byteFromBase16(traceId[18], traceId[19]) shl 48) or
            (byteFromBase16(traceId[20], traceId[21]) shl 40) or
            (byteFromBase16(traceId[22], traceId[23]) shl 32) or
            (byteFromBase16(traceId[24], traceId[25]) shl 24) or
            (byteFromBase16(traceId[26], traceId[27]) shl 16) or
            (byteFromBase16(traceId[28], traceId[29]) shl 8) or
            byteFromBase16(traceId[30], traceId[31])

    private fun byteFromBase16(first: Char, second: Char): Long =
        ((first.digitToInt(16) shl 4) or second.digitToInt(16)).toLong()
}
