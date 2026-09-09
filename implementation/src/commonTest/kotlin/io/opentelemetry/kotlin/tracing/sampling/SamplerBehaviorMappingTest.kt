package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.init.SamplerConfigDsl
import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceFlagsImpl
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SamplerBehaviorMappingTest {

    private val idGenerator = IdGeneratorImpl()
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory =
        SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory)
    private val spanFactory = SpanFactoryImpl(spanContextFactory)
    private val contextFactory = ContextFactoryImpl(spanFactory)

    private val samplerDsl = object : SamplerConfigDsl {
        override val spanFactory = this@SamplerBehaviorMappingTest.spanFactory
    }

    private fun contextWithParent(sampled: Boolean, isRemote: Boolean): Context {
        val traceFlags = if (sampled) {
            traceFlagsFactory.default
        } else {
            TraceFlagsImpl(isSampled = false, isRandom = false)
        }
        val parentSpanContext = spanContextFactory.create(
            traceId = "12345678901234567890123456789012",
            spanId = "1234567890123456",
            traceFlags = traceFlags,
            traceState = traceStateFactory.default,
            isRemote = isRemote,
        )
        val parentSpan = NonRecordingSpan(spanContextFactory.invalid, parentSpanContext)
        return contextFactory.root().storeSpan(parentSpan)
    }

    private fun sample(
        sampler: Sampler,
        context: Context = contextFactory.root(),
    ): SamplingResult = sampler.shouldSample(
        context = context,
        traceIdBytes = ZERO_TRACE_ID,
        name = "span",
        spanKind = SpanKind.INTERNAL,
        attributes = AttributesModel(),
        links = emptyList(),
    )

    @Test
    fun alwaysOnMapsToAlwaysOnSampler() {
        val sampler = samplerDsl.toSampler(SamplerBehavior.AlwaysOn)
        assertEquals("AlwaysOnSampler", sampler.description)
        assertEquals(Decision.RECORD_AND_SAMPLE, sample(sampler).decision)
    }

    @Test
    fun alwaysOffMapsToAlwaysOffSampler() {
        val sampler = samplerDsl.toSampler(SamplerBehavior.AlwaysOff)
        assertEquals("AlwaysOffSampler", sampler.description)
        assertEquals(Decision.DROP, sample(sampler).decision)
    }

    @Test
    fun emptyParentBasedUsesSchemaChildDefaults() {
        val sampler = samplerDsl.toSampler(SamplerBehavior.ParentBased())
        assertEquals(parentBasedDescription(root = "AlwaysOnSampler"), sampler.description)
    }

    @Test
    fun omittedParentBasedChildrenKeepDefaults() {
        val sampler = samplerDsl.toSampler(
            SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOff)
        )
        assertEquals(parentBasedDescription(root = "AlwaysOffSampler"), sampler.description)
    }

    @Test
    fun invertedParentBasedChildrenAreMapped() {
        val sampler = samplerDsl.toSampler(
            SamplerBehavior.ParentBased(
                root = SamplerBehavior.AlwaysOff,
                remoteParentSampled = SamplerBehavior.AlwaysOff,
                remoteParentNotSampled = SamplerBehavior.AlwaysOn,
                localParentSampled = SamplerBehavior.AlwaysOff,
                localParentNotSampled = SamplerBehavior.AlwaysOn,
            )
        )

        assertEquals(
            parentBasedDescription(
                root = "AlwaysOffSampler",
                remoteParentSampled = "AlwaysOffSampler",
                remoteParentNotSampled = "AlwaysOnSampler",
                localParentSampled = "AlwaysOffSampler",
                localParentNotSampled = "AlwaysOnSampler",
            ),
            sampler.description
        )
        assertEquals(Decision.DROP, sample(sampler).decision)
        assertEquals(
            Decision.DROP,
            sample(sampler, contextWithParent(sampled = true, isRemote = true)).decision,
        )
        assertEquals(
            Decision.RECORD_AND_SAMPLE,
            sample(sampler, contextWithParent(sampled = false, isRemote = true)).decision,
        )
        assertEquals(
            Decision.DROP,
            sample(sampler, contextWithParent(sampled = true, isRemote = false)).decision,
        )
        assertEquals(
            Decision.RECORD_AND_SAMPLE,
            sample(sampler, contextWithParent(sampled = false, isRemote = false)).decision,
        )
    }

    @Test
    fun nestedParentBasedMapsRecursively() {
        val sampler = samplerDsl.toSampler(
            SamplerBehavior.ParentBased(
                root = SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOff)
            )
        )

        assertEquals(
            parentBasedDescription(
                root = parentBasedDescription(root = "AlwaysOffSampler")
            ),
            sampler.description
        )
        assertEquals(Decision.DROP, sample(sampler).decision)
        assertEquals(
            Decision.RECORD_AND_SAMPLE,
            sample(sampler, contextWithParent(sampled = true, isRemote = true)).decision,
        )
    }

    private companion object {
        val ZERO_TRACE_ID = "00000000000000000000000000000000".hexToByteArray()

        fun parentBasedDescription(
            root: String,
            remoteParentSampled: String = "AlwaysOnSampler",
            remoteParentNotSampled: String = "AlwaysOffSampler",
            localParentSampled: String = "AlwaysOnSampler",
            localParentNotSampled: String = "AlwaysOffSampler",
        ): String =
            "ParentBased{" +
                    "root:$root," +
                    "remoteParentSampled:$remoteParentSampled," +
                    "remoteParentNotSampled:$remoteParentNotSampled," +
                    "localParentSampled:$localParentSampled," +
                    "localParentNotSampled:$localParentNotSampled" +
                    "}"
    }
}
