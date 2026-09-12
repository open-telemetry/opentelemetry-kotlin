package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.config.envar.EnvVarReader
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.sampling.ParentBasedSampler
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision
import io.opentelemetry.kotlin.tracing.sampling.alwaysOn
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalStdlibApi::class)
internal class ResolvedSamplerConfigFileTest {

    private val clock = FakeClock()
    private val idGenerator = IdGeneratorImpl()
    private val spanContextFactory = SpanContextFactoryImpl(idGenerator)
    private val spanFactory = SpanFactoryImpl(spanContextFactory)
    private val contextFactory = ContextFactoryImpl(spanFactory)

    private fun samplerOf(
        getEnvVar: (String) -> String? = { null },
        configYaml: String? = null,
        configure: TracerProviderConfigDsl.() -> Unit = {},
    ): Sampler {
        val cfg = OpenTelemetryConfigImpl(
            clock,
            envVarReader = EnvVarReader(getEnvVar),
        ).apply {
            if (configYaml != null) {
                configFile(writeConfigFile(configYaml))
            }
            tracerProvider(configure)
        }
        return cfg.generateTracingConfig().samplerFactory(spanFactory)
    }

    private fun Sampler.shouldSampleRoot(): Decision = shouldSample(
        context = contextFactory.root(),
        traceIdBytes = ZERO_TRACE_ID,
        name = "root",
        spanKind = SpanKind.INTERNAL,
        attributes = AttributesModel(),
        links = emptyList()
    ).decision

    private fun env(sampler: String): (String) -> String? {
        val values = buildMap {
            put("OTEL_TRACES_SAMPLER", sampler)
        }
        return values::get
    }

    @Test
    fun configFileSamplerIsAppliedWhenDslOmitsSampler() {
        val sampler = samplerOf(configYaml = ALWAYS_OFF_SAMPLER_FILE)
        assertEquals("AlwaysOffSampler", sampler.description)
        assertEquals(Decision.DROP, sampler.shouldSampleRoot())
    }

    @Test
    fun emptyConfigFileReplacesEnvSampler() {
        val sampler = samplerOf(
            getEnvVar = env("always_off"),
            configYaml = EMPTY_CONFIG_FILE,
        )
        val parentBased = assertIs<ParentBasedSampler>(sampler)
        assertContains(parentBased.description, "root:AlwaysOnSampler")
        assertEquals(Decision.RECORD_AND_SAMPLE, sampler.shouldSampleRoot())
    }

    @Test
    fun configFileBeatsEnvWhenDslOmitsSampler() {
        val sampler = samplerOf(
            getEnvVar = env("always_on"),
            configYaml = ALWAYS_OFF_SAMPLER_FILE,
        )
        assertEquals("AlwaysOffSampler", sampler.description)
        assertEquals(Decision.DROP, sampler.shouldSampleRoot())
    }

    @Test
    fun dslSamplerWinsOverConfigFile() {
        val sampler = samplerOf(
            getEnvVar = env("always_off"),
            configYaml = ALWAYS_OFF_SAMPLER_FILE,
        ) {
            sampler { alwaysOn() }
        }
        assertEquals("AlwaysOnSampler", sampler.description)
        assertEquals(Decision.RECORD_AND_SAMPLE, sampler.shouldSampleRoot())
    }

    private fun writeConfigFile(contents: String): String {
        val file = File.createTempFile("opentelemetry-config", ".yaml")
        file.deleteOnExit()
        file.writeText(contents)
        return file.absolutePath
    }

    private companion object {
        val ZERO_TRACE_ID = "00000000000000000000000000000000".hexToByteArray()
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
