package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.SamplerBehavior
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
}
