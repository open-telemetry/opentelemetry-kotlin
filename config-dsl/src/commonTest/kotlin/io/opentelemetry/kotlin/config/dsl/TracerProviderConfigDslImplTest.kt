package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TracerProviderConfigDslImplTest {

    @Test
    fun processorStartsUnset() {
        assertNull(TracerProviderConfigDslImpl().toBehavior().processor)
    }

    @Test
    fun samplerStartsUnset() {
        assertNull(TracerProviderConfigDslImpl().toBehavior().sampler)
    }

    @Test
    fun samplerCallSetsSampler() {
        val dsl = TracerProviderConfigDslImpl()
        dsl.sampler { alwaysOn() }
        assertEquals(SamplerBehavior.AlwaysOn, dsl.toBehavior().sampler)
    }

    @Test
    fun samplerMapsParentBased() {
        val dsl = TracerProviderConfigDslImpl()
        dsl.sampler { parentBased(root = alwaysOff()) }
        assertEquals(
            SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOff),
            dsl.toBehavior().sampler
        )
    }

    @Test
    fun samplerDoesNotDropProcessor() {
        val dsl = TracerProviderConfigDslImpl()
        dsl.export { error("behavior mapping does not run the export lambda") }
        dsl.sampler { alwaysOff() }

        val behavior = dsl.toBehavior()
        assertEquals(SpanProcessorBehavior(), behavior.processor)
        assertEquals(SamplerBehavior.AlwaysOff, behavior.sampler)
    }

    @Test
    fun exportCallSetsProcessor() {
        val dsl = TracerProviderConfigDslImpl()
        dsl.export { error("behavior mapping does not run the export lambda") }

        assertEquals(
            SpanProcessorBehavior(),
            dsl.toBehavior().processor,
        )
    }

    @Test
    fun processorCallSetsSimple() {
        val dsl = TracerProviderConfigDslImpl()
        dsl.processor { simple() }

        assertEquals(
            SpanProcessorBehavior(simple = SimpleSpanProcessorBehavior()),
            dsl.toBehavior().processor,
        )
    }

    @Test
    fun processorCallSetsBatch() {
        val dsl = TracerProviderConfigDslImpl()
        dsl.processor { batch() }

        assertEquals(
            SpanProcessorBehavior(batch = BatchSpanProcessorBehavior()),
            dsl.toBehavior().processor,
        )
    }

    @Test
    fun exportKeepsProcessorAlreadyChosen() {
        val dsl = TracerProviderConfigDslImpl()
        dsl.processor { simple() }
        dsl.export { error("behavior mapping does not run the export lambda") }

        assertEquals(
            SpanProcessorBehavior(simple = SimpleSpanProcessorBehavior()),
            dsl.toBehavior().processor,
        )
    }

    @Test
    fun processorDoesNotDropSampler() {
        val dsl = TracerProviderConfigDslImpl()
        dsl.processor { simple() }
        dsl.sampler { alwaysOff() }

        val behavior = dsl.toBehavior()
        assertEquals(
            SpanProcessorBehavior(simple = SimpleSpanProcessorBehavior()),
            behavior.processor,
        )
        assertEquals(SamplerBehavior.AlwaysOff, behavior.sampler)
    }
}
