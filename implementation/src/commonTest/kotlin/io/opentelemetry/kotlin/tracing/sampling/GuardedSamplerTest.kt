package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.EmptyAttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.init.config.DefaultSampler
import io.opentelemetry.kotlin.tracing.FakeTraceState
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class GuardedSamplerTest {

    private val errorHandler = FakeSdkErrorHandler()
    private val fallback = FakeSampler(Decision.DROP, samplerTraceState = FakeTraceState(mapOf("fallback" to "used")))

    @Test
    fun testCopiesDelegateResultWhenHealthy() = runTest {
        val delegate = FakeSampler(
            samplerAttributes = mapOf("key" to "value"),
            samplerTraceState = FakeTraceState(mapOf("vendor" to "state")),
            shutdownCode = { OperationResultCode.Failure },
        )
        val sampler = GuardedSampler(delegate, errorHandler, fallback)
        val result = sampler.sample()

        assertEquals(Decision.RECORD_AND_SAMPLE, result.decision)
        assertEquals(mapOf("key" to "value"), result.attributes.attributes)
        assertEquals(mapOf("vendor" to "state"), result.traceState.asMap())
        assertEquals("FakeSampler", sampler.description)
        assertEquals(OperationResultCode.Failure, sampler.shutdown())
        assertEquals(1, delegate.shutdownCount)
        assertEquals(0, fallback.callCount)
        assertTrue(errorHandler.errors.isEmpty())
    }

    @Test
    fun testFallsBackWhenSamplingFails() {
        val hostileTraceState = object : TraceState by FakeTraceState() {
            override fun asMap(): Map<String, String> = boom()
        }
        listOf(
            TestSampler { boom() },
            TestSampler { TestResult(decisionFn = { boom() }) },
            TestSampler { TestResult(traceStateFn = { boom() }) },
            TestSampler { TestResult(traceStateFn = { hostileTraceState }) },
        ).forEachIndexed { index, delegate ->
            val result = GuardedSampler(delegate, errorHandler, fallback).sample()

            assertEquals(Decision.DROP, result.decision, "case $index")
            assertEquals(mapOf("fallback" to "used"), result.traceState.asMap(), "case $index")
        }
        assertEquals(4, fallback.callCount)
        assertEquals(4, errorHandler.userCodeErrors.count { it.message == "Sampler failed, using fallback sampler" })

        val sampler = GuardedSampler(TestSampler(descriptionFn = { boom() }), errorHandler, fallback)
        assertEquals("FakeSampler", sampler.description)
        assertEquals("Sampler.description failed", errorHandler.userCodeErrors.last().message)
    }

    @Test
    fun testAttributesFailureKeepsDecision() {
        val hostileAttributes = TestResult(attributesFn = { boom() })
        val sampled = GuardedSampler(TestSampler { hostileAttributes }, errorHandler, fallback).sample()
        assertEquals(Decision.RECORD_AND_SAMPLE, sampled.decision)
        assertSame(EmptyAttributeContainer, sampled.attributes)
        assertEquals("SamplingResult.attributes failed", errorHandler.userCodeErrors.single().message)

        val hostileDropped = TestResult(decisionFn = { Decision.DROP }, attributesFn = { boom() })
        val dropped = GuardedSampler(TestSampler { hostileDropped }, errorHandler, fallback).sample()
        assertEquals(Decision.DROP, dropped.decision)
        assertEquals(1, errorHandler.errors.size)
        assertEquals(0, fallback.callCount)
    }

    @Test
    fun testGuardedSkipsBuiltInSamplers() {
        listOf(AlwaysOnSampler, AlwaysOffSampler, DefaultSampler).forEach { assertSame(it, it.guarded(errorHandler)) }
        val guarded = assertIs<GuardedSampler>(FakeSampler().guarded(errorHandler))
        assertSame(guarded, guarded.guarded(errorHandler))
    }

    private fun Sampler.sample(): SamplingResult =
        shouldSample(rootContext(), ByteArray(16) { 1 }, "span", SpanKind.INTERNAL, EmptyAttributeContainer, emptyList())

    private class TestResult(
        private val decisionFn: () -> Decision = { Decision.RECORD_AND_SAMPLE },
        private val attributesFn: () -> AttributeContainer = { EmptyAttributeContainer },
        private val traceStateFn: () -> TraceState = { FakeTraceState(emptyMap()) },
    ) : SamplingResult {
        override val decision: Decision get() = decisionFn()
        override val attributes: AttributeContainer get() = attributesFn()
        override val traceState: TraceState get() = traceStateFn()
    }

    private class TestSampler(
        private val descriptionFn: () -> String = { "TestSampler" },
        private val resultFn: () -> SamplingResult = { TestResult() },
    ) : Sampler {
        override val description: String get() = descriptionFn()

        override fun shouldSample(
            context: Context,
            traceIdBytes: ByteArray,
            name: String,
            spanKind: SpanKind,
            attributes: AttributeContainer,
            links: List<SpanLink>,
        ): SamplingResult = resultFn()
    }
}

private fun boom(): Nothing = error("boom")
