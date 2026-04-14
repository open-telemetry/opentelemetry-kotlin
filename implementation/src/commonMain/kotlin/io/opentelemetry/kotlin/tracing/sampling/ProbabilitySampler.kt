package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision

internal class ProbabilitySampler(private val spanFactory: SpanFactory, ratio: Double) : Sampler {

    init {
        require(ratio in 0.0..1.0) { "ratio must be between 0.0 and 1.0, got $ratio" }
    }

    private companion object {
        private const val MAX_THRESHOLD = 1L shl 56
    }

    private val rejectionThreshold: Long = ((1 - ratio.coerceAtLeast(1.0 / MAX_THRESHOLD)) * MAX_THRESHOLD).toLong()

    override val description: String = "ProbabilitySampler{$ratio}"

    override fun shouldSample(
        context: Context,
        traceId: String,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>
    ): SamplingResult {
        val decision = if (randomnessFromTraceId(traceId) >= rejectionThreshold) {
            Decision.RECORD_AND_SAMPLE
        } else {
            Decision.DROP
        }
        return SamplingResultImpl(
            decision = decision,
//            TODO: should this get fresh attributes?
            attributes = attributes,
            traceState = spanFactory.fromContext(context).spanContext.traceState,
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

    private fun byteFromBase16(first: Char, second: Char): Long = ((first.digitToInt(16) shl 4) or second.digitToInt(16)).toLong()
}
