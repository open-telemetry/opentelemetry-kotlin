package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.config.envar.EnvVarReader
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.tracing.sampling.FakeSampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult
import io.opentelemetry.kotlin.tracing.sampling.alwaysOn
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CompatResolvedSamplerConfigTest {

    private val clock = FakeClock()
    private val idGenerator = CompatIdGenerator()
    private val noGlobalLimits = AttributeLimitsBehavior()

    private val noSpanLimits = SpanLimitsBehavior()

    private fun startSpan(
        getEnvVar: (String) -> String? = { null },
        configYaml: String? = null,
        errorHandler: SdkErrorHandler? = null,
        configure: TracerProviderConfigDsl.() -> Unit = {},
    ) = CompatOpenTelemetryConfig(
        clock,
        envVarReader = EnvVarReader(getEnvVar),
    ).apply {
        if (errorHandler != null) {
            errorHandler(errorHandler)
        }
        if (configYaml != null) {
            configFile(writeConfigFile(configYaml))
        }
        tracerProvider(configure)
        applyResolvedSampler()
    }.tracerProviderConfig.build(clock, idGenerator, globalLimits = noGlobalLimits, spanLimits = noSpanLimits)
        .getTracer("test")
        .startSpan("span")

    private fun env(sampler: String): (String) -> String? {
        val values = buildMap {
            put("OTEL_TRACES_SAMPLER", sampler)
        }
        return values::get
    }

    private fun writeConfigFile(contents: String): String {
        val file = File.createTempFile("opentelemetry-config", ".yaml")
        file.deleteOnExit()
        file.writeText(contents)
        return file.absolutePath
    }

    /** Unset env/file: Java default ParentBased(AlwaysOn) samples roots. */
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

    /** configFile() YAML sampler is applied when DSL omits sampler. */
    @Test
    fun configFileSamplerIsAppliedWhenDslOmitsSampler() {
        val span = startSpan(configYaml = ALWAYS_OFF_SAMPLER_FILE)
        assertFalse(span.isRecording())
    }

    /**
     * A config file, even without a sampler, replaces env
     * (BehaviorResolver: file ?: env).
     */
    @Test
    fun emptyConfigFileReplacesEnvSampler() {
        val span = startSpan(
            getEnvVar = env("always_off"),
            configYaml = EMPTY_CONFIG_FILE,
        )
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    /** Non-empty config file beats env when DSL omits sampler (File > Env). */
    @Test
    fun configFileBeatsEnvWhenDslOmitsSampler() {
        val span = startSpan(
            getEnvVar = env("always_on"),
            configYaml = ALWAYS_OFF_SAMPLER_FILE,
        )
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    /** DSL outranks file and env. */
    @Test
    fun dslSamplerWinsOverConfigFile() {
        val span = startSpan(
            getEnvVar = env("always_off"),
            configYaml = ALWAYS_OFF_SAMPLER_FILE,
        ) {
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
        val EMPTY_CONFIG_FILE = """
            file_format: "1.0"
        """.trimIndent()
        val ALWAYS_OFF_SAMPLER_FILE = """
            file_format: "1.0"
            tracer_provider:
              processors: []
              sampler:
                always_off: {}
        """.trimIndent()
    }
}
