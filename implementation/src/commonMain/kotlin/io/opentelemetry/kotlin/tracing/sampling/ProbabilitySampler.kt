package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.EmptyAttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision
import kotlin.concurrent.Volatile

internal class ProbabilitySampler(
    ratio: Double,
    private val sdkErrorHandler: SdkErrorHandler,
) : Sampler {

    private companion object {
        @Volatile
        private var compatibilityWarningLogged = false
        const val COMPATIBILITY_WARNING = "WARNING: The ProbabilitySampler sampler is presuming TraceIDs are random " +
            "and expects the Trace random flag to be set in confirmation. Please " +
            "upgrade your caller(s) to use W3C Trace Context Level 2."
    }

    init {
        validateRatio(ratio)
    }

    private val rejectionThreshold: Long = thresholdFromRatio(ratio)

    override val description: String = "ProbabilitySampler{$ratio}"

    override fun shouldSample(
        context: Context,
        traceIdBytes: ByteArray,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>
    ): SamplingResult {
        val parentSpanContext = context.span.spanContext
        val traceState = parentSpanContext.traceState
        val otelTraceState = OtelTraceState.parse(traceState.get(KnownTraceState.OT))

        val explicitRandomness = otelTraceState.rv
        val randomness = if (explicitRandomness != null) {
            explicitRandomness
        } else {
            if (parentSpanContext.isValid && !parentSpanContext.traceFlags.isRandom && !compatibilityWarningLogged) {
                compatibilityWarningLogged = true
                sdkErrorHandler.reportError(
                    SdkError.ApiMisuse(
                        api = "ProbabilitySampler.shouldSample",
                        message = COMPATIBILITY_WARNING,
                        severity = SdkErrorSeverity.WARNING,
                    )
                )
            }
            randomnessFromTraceIdBytes(traceIdBytes)
        }

        val incomingTh = otelTraceState.th
        if (
            incomingTh != null &&
            parentSpanContext.isValid &&
            (randomness >= incomingTh) != parentSpanContext.traceFlags.isSampled
        ) {
            otelTraceState.eraseThreshold()
        }

        val decision = if (randomness >= rejectionThreshold) {
            otelTraceState.setThreshold(rejectionThreshold)
            Decision.RECORD_AND_SAMPLE
        } else {
            otelTraceState.eraseThreshold()
            Decision.DROP
        }

        val encoded = otelTraceState.encode()
        return SamplingResultImpl(
            decision = decision,
            attributes = EmptyAttributeContainer,
            traceState = if (encoded.isEmpty()) {
                traceState.remove(KnownTraceState.OT)
            } else {
                traceState.put(KnownTraceState.OT, encoded)
            },
        )
    }
}
