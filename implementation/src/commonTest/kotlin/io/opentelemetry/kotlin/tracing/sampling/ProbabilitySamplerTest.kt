package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.internalRootContext
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.createSpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalApi::class, TempContextStandInApi::class)
internal class ProbabilitySamplerTest {

    private val idGenerator = IdGeneratorImpl()
    private val sdkErrorHandler = NoopSdkErrorHandler

    @Test
    fun testDecisionUsesThresholdQuantizedFromOriginalProbability() {
        val sampler = ProbabilitySampler(65544.25 / (1L shl 56), sdkErrorHandler)
        val cases = mapOf(
            "000000000000000000fffffffffefff0" to SamplingResult.Decision.RECORD_AND_SAMPLE,
            "000000000000000000fffffffffeffef" to SamplingResult.Decision.DROP,
        )
        for ((traceId, expected) in cases) {
            val result = sampler.shouldSample(
                context = internalRootContext(),
                traceIdBytes = traceId.hexToByteArray(),
                name = "span",
                spanKind = SpanKind.INTERNAL,
                attributes = AttributesModel(),
                links = emptyList(),
            )
            assertEquals(expected, result.decision)
            assertEquals("th:fffffffffefff", result.traceState.get("ot"))
        }
    }

    @Test
    fun testDecisionUsesEncodedThresholdPrecision() {
        val sampler = ProbabilitySampler(0.1, sdkErrorHandler)
        val cases = mapOf(
            "000000000000000000e6660000000000" to SamplingResult.Decision.RECORD_AND_SAMPLE,
            "000000000000000000e665ffffffffff" to SamplingResult.Decision.DROP,
        )
        for ((traceId, expected) in cases) {
            val result = sampler.shouldSample(
                context = internalRootContext(),
                traceIdBytes = traceId.hexToByteArray(),
                name = "span",
                spanKind = SpanKind.INTERNAL,
                attributes = AttributesModel(),
                links = emptyList(),
            )
            assertEquals(expected, result.decision)
            assertEquals("th:e666", result.traceState.get("ot"))
        }
    }

    private fun createParentContext(traceId: String, otTraceStateValue: String): Context {
        val parentSpanContext = createSpanContext(traceId.hexToByteArray(), idGenerator.generateSpanIdBytes()) {
            isSampled = true
            isRemote = true
            traceState { put("ot", otTraceStateValue) }
        }
        return internalRootContext().storeSpanContext(parentSpanContext)
    }

    @Test
    fun testRecordsAndSamplesSpan() {
        val result = ProbabilitySampler(0.5, sdkErrorHandler).shouldSample(
            context = internalRootContext(),
            traceIdBytes = "000000000000000000ffffffffffffff".hexToByteArray(),
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
    }

    @Test
    fun testDropsSpan() {
        val result = ProbabilitySampler(0.5, sdkErrorHandler).shouldSample(
            context = internalRootContext(),
            traceIdBytes = "ffffffffffffffffff00000000000000".hexToByteArray(),
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.DROP, result.decision)
    }

    @Test
    fun testRecordsAndSamplesSpanAtMinimumRatio() {
        val ratio = 1.0 / (1L shl 56).toDouble()
        val result = ProbabilitySampler(ratio, sdkErrorHandler).shouldSample(
            context = internalRootContext(),
            traceIdBytes = "000000000000000000ffffffffffffff".hexToByteArray(),
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
    }

    @Test
    fun testDropsSpanAtMinimumRatio() {
        val ratio = 1.0 / (1L shl 56).toDouble()
        val result = ProbabilitySampler(ratio, sdkErrorHandler).shouldSample(
            context = internalRootContext(),
            traceIdBytes = "000000000000000000fffffffffffffe".hexToByteArray(),
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.DROP, result.decision)
    }

    @Test
    fun testThrowsOnInvalidRatio() {
        assertFailsWith(IllegalArgumentException::class) {
            ProbabilitySampler(0.0, sdkErrorHandler)
        }
        assertFailsWith(IllegalArgumentException::class) {
            ProbabilitySampler(2.0, sdkErrorHandler)
        }
    }

    @Test
    fun recordsAndSamplesSpanWithExplicitRandomness() {
        val ratio = 0.5 // 0x80000000000000
        val belowThreshold = 70000000000000 // span would be dropped if this randomness value were used
        val aboveThreshold = 90000000000000 // span would be sampled if this randomness value were used
        val traceId = "aaaaaaaaaaaaaaaaaa$belowThreshold"
        val context = createParentContext(
            traceId = traceId,
            otTraceStateValue = "rv:$aboveThreshold",
        )
        val result = ProbabilitySampler(ratio, sdkErrorHandler).shouldSample(
            context = context,
            traceIdBytes = traceId.hexToByteArray(),
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
    }

    @Test
    fun fallsBackToTraceIdRandomness() {
        val traceId = "aaaaaaaaaaaaaaaaaa90000000000000"
        val context = createParentContext(
            traceId = traceId,
            otTraceStateValue = "rv:garbage",
        )
        val result = ProbabilitySampler(0.5, sdkErrorHandler).shouldSample(
            context = context,
            traceIdBytes = traceId.hexToByteArray(),
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
    }

    @Test
    fun updatesExistingThreshold() {
        val traceId = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        val context = createParentContext(
            traceId = traceId,
            otTraceStateValue = "th:123",
        )
        val result = ProbabilitySampler(0.5, sdkErrorHandler).shouldSample(
            context = context,
            traceIdBytes = traceId.hexToByteArray(),
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals("th:8", result.traceState.get("ot"))
    }

    @Test
    fun erasesInconsistentThreshold() {
        val traceId = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        val context = createParentContext(
            traceId = traceId,
            otTraceStateValue = "rv:a0000000000000;th:c", // rv < th
        )
        val result = ProbabilitySampler(0.5, sdkErrorHandler).shouldSample(
            context = context,
            traceIdBytes = traceId.hexToByteArray(),
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals("rv:a0000000000000;th:8", result.traceState.get("ot"))
    }
}
