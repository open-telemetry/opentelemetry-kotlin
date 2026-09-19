package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.config.envar.EnvVarReader
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.sampling.FakeSampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult
import io.opentelemetry.kotlin.tracing.sampling.alwaysOn
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CompatResolvedSamplerConfigTest {

    private val clock = FakeClock()
    private val contextFactory = CompatContextFactory()

    private fun startSpan(
        getEnvVar: (String) -> String? = { null },
        resolved: OpenTelemetryBehavior? = null,
        errorHandler: SdkErrorHandler? = null,
        configure: TracerProviderConfigDsl.() -> Unit = {},
    ): Span {
        val cfg = CompatOpenTelemetryConfig(clock).apply {
            if (errorHandler != null) {
                errorHandler(errorHandler)
            }
            tracerProvider(configure)
        }
        val behavior = resolved ?: defaultCompatBehaviorReader(
            envVarReader = EnvVarReader(getEnvVar),
            sdkErrorHandler = cfg.sdkErrorHandler,
        ).read(cfg.configFilePath, cfg.toBehavior())
        return CompatSdkConfigFactory(cfg, behavior, clock, contextFactory)
            .buildTracerProvider()
            .getTracer("test")
            .startSpan("span")
    }

    private fun env(sampler: String): (String) -> String? {
        val values = buildMap {
            put("OTEL_TRACES_SAMPLER", sampler)
        }
        return values::get
    }

    /** Unset env: Java default ParentBased(AlwaysOn) samples roots. */
    @Test
    fun unsetLayersLeaveTheSdkDefaultSampler() {
        val span = startSpan()
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    /** OTEL_TRACES_SAMPLER=always_off is applied when sampler { } is omitted. */
    @Test
    fun envAlwaysOffIsAppliedWhenDslOmitsSampler() {
        val span = startSpan(getEnvVar = env("always_off"))
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    /** ALWAYS_ON is accepted (env names are case-insensitive). */
    @Test
    fun envSamplerNameIsCaseInsensitive() {
        val span = startSpan(getEnvVar = env("ALWAYS_OFF"))
        assertFalse(span.isRecording())
    }

    /** OTEL_TRACES_SAMPLER=parentbased_always_on applies ParentBased(AlwaysOn root). */
    @Test
    fun envParentBasedAlwaysOnIsAppliedWhenDslOmitsSampler() {
        val span = startSpan(getEnvVar = env("parentbased_always_on"))
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    /** OTEL_TRACES_SAMPLER=parentbased_always_off applies ParentBased(AlwaysOff root). */
    @Test
    fun envParentBasedAlwaysOffIsAppliedWhenDslOmitsSampler() {
        val span = startSpan(getEnvVar = env("parentbased_always_off"))
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    /** sampler { alwaysOn() } outranks OTEL_TRACES_SAMPLER=always_off. */
    @Test
    fun dslSamplerWinsOverEnv() {
        val span = startSpan(getEnvVar = env("always_off")) {
            sampler { alwaysOn() }
        }
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    /**
     * Custom DSL sampler is not replaced by env.
     * RECORD_ONLY → recording but not sampled; always_on would also set sampled.
     */
    @Test
    fun dslCustomSamplerIsNotReplacedByEnv() {
        val span = startSpan(getEnvVar = env("always_on")) {
            sampler { FakeSampler(SamplingResult.Decision.RECORD_ONLY) }
        }
        assertTrue(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    /** Resolved AlwaysOff behavior is applied when DSL omits sampler. */
    @Test
    fun resolvedAlwaysOffIsAppliedWhenDslOmitsSampler() {
        val span = startSpan(resolved = alwaysOffBehavior())
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    /** DSL outranks already-resolved AlwaysOff behavior. */
    @Test
    fun dslSamplerWinsOverResolvedBehavior() {
        val span = startSpan(resolved = alwaysOffBehavior()) {
            sampler { alwaysOn() }
        }
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    /** Unknown OTEL_TRACES_SAMPLER reports warning and keeps Java default (samples roots). */
    @Test
    fun invalidEnvSamplerReportsWarningAndKeepsDefault() {
        val handler = FakeSdkErrorHandler()
        val span = startSpan(
            getEnvVar = env("not_a_sampler"),
            errorHandler = handler,
        )

        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
        assertEquals(1, handler.apiMisuses.size)

        val misuse = handler.apiMisuses.single()
        assertEquals("OTEL_TRACES_SAMPLER", misuse.api)
        assertContains(misuse.message, "not_a_sampler")
        assertEquals(SdkErrorSeverity.WARNING, misuse.severity)
    }

    private companion object {
        fun alwaysOffBehavior() = OpenTelemetryBehavior(
            tracerProvider = TracerProviderBehavior(sampler = SamplerBehavior.AlwaysOff),
        )
    }
}
