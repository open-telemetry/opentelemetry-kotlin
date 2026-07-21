package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.platformLog
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision
import kotlin.concurrent.Volatile

internal class ProbabilitySampler(ratio: Double) : Sampler {

    private companion object {
        @Volatile
        private var compatibilityWarningLogged = false
        const val COMPATIBILITY_WARNING = "WARNING: The ProbabilitySampler sampler is presuming TraceIDs are random " +
            "and expects the Trace random flag to be set in confirmation. Please " +
            "upgrade your caller(s) to use W3C Trace Context Level 2."
    }

    init {
        require(ratio in (1.0 / Threshold.MAX)..1.0) { "ratio must be between 2^-56 and 1, got $ratio" }
    }

    private val rejectionThreshold = Threshold.fromRatio(ratio)

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
            Randomness.fromTraceId(traceId)
        }

        val incomingTh = otelTraceState.th
        if (
            incomingTh != null &&
            parentSpanContext.isValid &&
            (randomness >= incomingTh) != parentSpanContext.traceFlags.isSampled
        ) {
            otelTraceState.eraseThreshold()
        }

        otelTraceState.applyThreshold(rejectionThreshold)

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
}
