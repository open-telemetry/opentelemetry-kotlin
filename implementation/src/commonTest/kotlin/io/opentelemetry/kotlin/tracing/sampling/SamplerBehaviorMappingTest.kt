package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.init.SamplerConfigDsl
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceFlagsImpl
import io.opentelemetry.kotlin.tracing.TracerImpl
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.fakeSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalStdlibApi
internal class SamplerBehaviorMappingTest {

    private val clock = FakeClock()
    private val idGenerator = IdGeneratorImpl()
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory =
        SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory)
    private val spanFactory = SpanFactoryImpl(spanContextFactory)
    private val contextFactory = ContextFactoryImpl(spanFactory)
    private val scope = InstrumentationScopeInfoImpl("test", null, null, emptyMap())

    private val samplerDsl = object : SamplerConfigDsl {
        override val spanFactory = this@SamplerBehaviorMappingTest.spanFactory
    }

    private fun buildTracer(sampler: Sampler) = TracerImpl(
        clock = clock,
        processor = FakeSpanProcessor(),
        contextFactory = contextFactory,
        spanContextFactory = spanContextFactory,
        traceFlagsFactory = traceFlagsFactory,
        scope = scope,
        resource = FakeResource(),
        spanLimitConfig = fakeSpanLimitsConfig,
        idGenerator = idGenerator,
        shutdownState = MutableShutdownState(),
        sampler = sampler,
        sdkErrorHandler = NoopSdkErrorHandler,
    )

    private fun contextWithParent(sampled: Boolean, isRemote: Boolean): Context {
        val traceFlags = when {
            sampled -> traceFlagsFactory.default
            else -> TraceFlagsImpl(isSampled = false, isRandom = false)
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
        traceIdBytes: ByteArray = ZERO_TRACE_ID,
    ): SamplingResult = sampler.shouldSample(
        context = contextFactory.root(),
        traceIdBytes = traceIdBytes,
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
        assertEquals(
            "ParentBased{" +
                "root:AlwaysOnSampler," +
                "remoteParentSampled:AlwaysOnSampler," +
                "remoteParentNotSampled:AlwaysOffSampler," +
                "localParentSampled:AlwaysOnSampler," +
                "localParentNotSampled:AlwaysOffSampler" + "}",
            sampler.description
        )
    }

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

    @Test
    fun parentBasedHonorsRemoteSampledParent() {
        val sampler =
            samplerDsl.toSampler(SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOff))

        // simulate an incoming HTTP call from an upstream remote service (sampled = true, isRemote = true)
        val parentCtx = contextWithParent(sampled = true, isRemote = true)

        // start a child span in the tracer with that parent context
        val span = buildTracer(sampler).startSpan("child", parentContext = parentCtx)

        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun parentBasedMapsEveryChild() {
        val sampler = samplerDsl.toSampler(
            SamplerBehavior.ParentBased(
                root = SamplerBehavior.AlwaysOff,
                remoteParentSampled = SamplerBehavior.AlwaysOn,
                remoteParentNotSampled = SamplerBehavior.AlwaysOff,
                localParentSampled = SamplerBehavior.AlwaysOn,
                localParentNotSampled = SamplerBehavior.AlwaysOff
            )
        )

        assertEquals(
            (
                samplerDsl.parentBased(
                    root = samplerDsl.alwaysOff(),
                    remoteParentSampled = samplerDsl.alwaysOn(),
                    remoteParentNotSampled = samplerDsl.alwaysOff(),
                    localParentSampled = samplerDsl.alwaysOn(),
                    localParentNotSampled = samplerDsl.alwaysOff()
                ).description
                ),
            sampler.description
        )
    }

    private companion object {
        val ZERO_TRACE_ID = "00000000000000000000000000000000".hexToByteArray()
    }
}
