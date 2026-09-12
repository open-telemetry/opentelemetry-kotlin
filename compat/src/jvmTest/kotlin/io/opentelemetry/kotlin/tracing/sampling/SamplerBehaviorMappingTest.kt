package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.factory.CompatSpanContextFactory
import io.opentelemetry.kotlin.factory.CompatSpanFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.init.SamplerConfigDsl
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
internal class SamplerBehaviorMappingTest {

    private val samplerDsl = object : SamplerConfigDsl {
        override val spanFactory: SpanFactory = CompatSpanFactory(CompatSpanContextFactory())
    }

    /** AlwaysOn must map to Java's AlwaysOnSampler */
    @Test
    fun alwaysOnMapsToAlwaysOnSampler() {
        val sampler = samplerDsl.toSampler(SamplerBehavior.AlwaysOn)
        assertEquals("AlwaysOnSampler", sampler.description)
    }

    /** AlwaysOff must map to Java's AlwaysOffSampler */
    @Test
    fun alwaysOffMapsToAlwaysOffSampler() {
        val sampler = samplerDsl.toSampler(SamplerBehavior.AlwaysOff)
        assertEquals("AlwaysOffSampler", sampler.description)
    }

    /** Empty uses defaults */
    @Test
    fun emptyParentBasedUsesSchemaChildDefaults() {
        val sampler = samplerDsl.toSampler(SamplerBehavior.ParentBased())
        assertEquals(
            samplerDsl.parentBased(root = samplerDsl.alwaysOn()).description,
            sampler.description
        )
    }

    /** Omitted slots keep defaults */
    @Test
    fun omittedParentBasedChildrenKeepDefaults() {
        val sampler = samplerDsl.toSampler(
            SamplerBehavior.ParentBased(
                root = SamplerBehavior.AlwaysOff
            )
        )
        assertEquals(
            "ParentBased{" +
                "root:AlwaysOffSampler," +
                "remoteParentSampled:AlwaysOnSampler," +
                "remoteParentNotSampled:AlwaysOffSampler," +
                "localParentSampled:AlwaysOnSampler," +
                "localParentNotSampled:AlwaysOffSampler" + "}",
            sampler.description
        )
    }

    /** Integrity Check */
    @Test
    fun parentBasedMapsEveryChild() {
        val sampler = samplerDsl.toSampler(
            SamplerBehavior.ParentBased(
                root = SamplerBehavior.AlwaysOff,
                remoteParentSampled = SamplerBehavior.AlwaysOn,
                remoteParentNotSampled = SamplerBehavior.AlwaysOff,
                localParentSampled = SamplerBehavior.AlwaysOn,
                localParentNotSampled = SamplerBehavior.AlwaysOff,
            )
        )
        assertEquals(
            samplerDsl.parentBased(
                root = samplerDsl.alwaysOff(),
                remoteParentSampled = samplerDsl.alwaysOn(),
                remoteParentNotSampled = samplerDsl.alwaysOff(),
                localParentSampled = samplerDsl.alwaysOn(),
                localParentNotSampled = samplerDsl.alwaysOff(),
            ).description,
            sampler.description
        )
    }
}
