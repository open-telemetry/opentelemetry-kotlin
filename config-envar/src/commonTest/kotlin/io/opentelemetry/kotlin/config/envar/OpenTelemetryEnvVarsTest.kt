package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.ResourceBehavior
import io.opentelemetry.kotlin.behavior.SeverityLevel
import io.opentelemetry.kotlin.behavior.logging.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordLimitsBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.tracing.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.tracing.SamplerBehavior
import io.opentelemetry.kotlin.behavior.tracing.SpanExporterBehavior
import io.opentelemetry.kotlin.behavior.tracing.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.tracing.SpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.tracing.TracerProviderBehavior
import io.opentelemetry.kotlin.config.envar.logging.LogRecordExporterEnvVars
import io.opentelemetry.kotlin.config.envar.logging.LogRecordLimitsEnvVars
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import io.opentelemetry.kotlin.config.envar.tracing.SamplerEnvVars
import io.opentelemetry.kotlin.config.envar.tracing.SpanExporterEnvVars
import io.opentelemetry.kotlin.config.envar.tracing.SpanLimitsEnvVars
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OpenTelemetryEnvVarsTest {

    @Test
    fun emptyEnv() {
        val behavior = behaviorFrom(emptyMap())
        assertEquals(
            OpenTelemetryBehavior(
                disabled = false,
                attributeLimits = AttributeLimitsBehavior(),
                resource = ResourceBehavior(),
                loggerProvider = LoggerProviderBehavior(
                    logRecordLimits = LogRecordLimitsBehavior(),
                    processor = LogRecordProcessorBehavior(
                        batch = BatchLogRecordProcessorBehavior(
                            exporter = LogRecordExporterBehavior()
                        )
                    ),
                ),
                tracerProvider = TracerProviderBehavior(
                    spanLimits = SpanLimitsBehavior(),
                    processors = listOfNotNull(
                        SpanProcessorBehavior(
                        batch = BatchSpanProcessorBehavior(
                            exporter = SpanExporterBehavior()
                        )
                    )
                    )
                ),
            ),
            behavior,
        )
    }

    @Test
    fun globalAndLogRecordLimits() {
        val behavior = behaviorFrom(
            mapOf(
                AttributeLimitsEnvVars.ATTRIBUTE_COUNT_LIMIT to "64",
                AttributeLimitsEnvVars.ATTRIBUTE_VALUE_LENGTH_LIMIT to "256",
                LogRecordLimitsEnvVars.ATTRIBUTE_COUNT_LIMIT to "8",
            )
        )
        assertEquals(
            AttributeLimitsBehavior(attributeCountLimit = 64, attributeValueLengthLimit = 256),
            behavior.attributeLimits,
        )
        assertEquals(8, behavior.loggerProvider?.logRecordLimits?.attributeCountLimit)
    }

    @Test
    fun disallowedValueUnset() {
        val behavior = behaviorFrom(mapOf(AttributeLimitsEnvVars.ATTRIBUTE_COUNT_LIMIT to "-1"))
        assertNull(behavior.attributeLimits?.attributeCountLimit)
    }

    @Test
    fun `should read every node from its own env vars`() {
        val env = mapOf(
            OpenTelemetryEnvVars.SDK_DISABLED to "true",
            OpenTelemetryEnvVars.LOG_LEVEL to "ERROR3",
            AttributeLimitsEnvVars.ATTRIBUTE_COUNT_LIMIT to "1",
            AttributeLimitsEnvVars.ATTRIBUTE_VALUE_LENGTH_LIMIT to "2",
            SpanLimitsEnvVars.ATTRIBUTE_COUNT_LIMIT to "3",
            SpanLimitsEnvVars.ATTRIBUTE_VALUE_LENGTH_LIMIT to "4",
            SpanLimitsEnvVars.LINK_COUNT_LIMIT to "5",
            SpanLimitsEnvVars.EVENT_COUNT_LIMIT to "6",
            SpanLimitsEnvVars.EVENT_ATTRIBUTE_COUNT_LIMIT to "7",
            SpanLimitsEnvVars.LINK_ATTRIBUTE_COUNT_LIMIT to "8",
            LogRecordLimitsEnvVars.ATTRIBUTE_COUNT_LIMIT to "9",
            LogRecordLimitsEnvVars.ATTRIBUTE_VALUE_LENGTH_LIMIT to "10",
            ResourceEnvVars.RESOURCE_ATTRIBUTES to "region=eu",
            ResourceEnvVars.SERVICE_NAME to "checkout",
            SpanExporterEnvVars.TRACES_EXPORTER to Exporter.CONSOLE.value,
            LogRecordExporterEnvVars.LOGS_EXPORTER to Exporter.CONSOLE.value,
        )

        val expected = OpenTelemetryBehavior(
            disabled = true,
            logLevel = SeverityLevel.ERROR3,
            resource = ResourceBehavior(
                serviceName = "checkout",
                attributes = mapOf("region" to "eu"),
            ),
            attributeLimits = AttributeLimitsBehavior(
                attributeCountLimit = 1,
                attributeValueLengthLimit = 2,
            ),
            tracerProvider = TracerProviderBehavior(
                spanLimits = SpanLimitsBehavior(
                    attributeCountLimit = 3,
                    attributeValueLengthLimit = 4,
                    linkCountLimit = 5,
                    eventCountLimit = 6,
                    attributeCountPerEventLimit = 7,
                    attributeCountPerLinkLimit = 8,
                ),
                processors = listOfNotNull(SpanProcessorBehavior(
                    batch = BatchSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(
                            console = ConsoleExporterBehavior()
                        )
                    )
                ))
            ),
            loggerProvider = LoggerProviderBehavior(
                logRecordLimits = LogRecordLimitsBehavior(
                    attributeCountLimit = 9,
                    attributeValueLengthLimit = 10,
                ),
                processor = LogRecordProcessorBehavior(
                    batch = BatchLogRecordProcessorBehavior(
                        exporter = LogRecordExporterBehavior(console = ConsoleExporterBehavior())
                    )
                ),
            ),
        )
        assertEquals(expected, toBehavior(env::get))
    }

    @Test
    fun `should leave every limit unset when the environment configures nothing`() {
        val expected = OpenTelemetryBehavior(
            disabled = false,
            attributeLimits = AttributeLimitsBehavior(),
            resource = ResourceBehavior(),
            tracerProvider = TracerProviderBehavior(
                spanLimits = SpanLimitsBehavior(),
                processors = listOfNotNull(SpanProcessorBehavior(
                    batch = BatchSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(),
                    ),
                ))
            ),
            loggerProvider = LoggerProviderBehavior(
                logRecordLimits = LogRecordLimitsBehavior(),
                processor = LogRecordProcessorBehavior(
                    batch = BatchLogRecordProcessorBehavior(
                        exporter = LogRecordExporterBehavior(),
                    ),
                )
            ),

        )
        assertEquals(expected, toBehavior(getEnvVar = { null }))
    }

    @Test
    fun `should map sampler env vars`() {
        val env = mapOf(
            SamplerEnvVars.SAMPLER to SamplerEnvVars.ALWAYS_OFF,
        )
        val behavior = toBehavior(env::get)
        assertEquals(SamplerBehavior.AlwaysOff, behavior.tracerProvider?.sampler)
    }

    @Test
    fun `should leave sampler unset when OTEL_TRACES_SAMPLER is unset`() {
        assertNull(toBehavior(getEnvVar = { null }).tracerProvider?.sampler)
    }

    @Test
    fun `should forward warnings from invalid env vars`() {
        val env = mapOf(
            AttributeLimitsEnvVars.ATTRIBUTE_COUNT_LIMIT to "invalid",
            ResourceEnvVars.RESOURCE_ATTRIBUTES to "invalid",
            SamplerEnvVars.SAMPLER to "not_a_sampler",
            SpanExporterEnvVars.TRACES_EXPORTER to "not_an_exporter",
            LogRecordExporterEnvVars.LOGS_EXPORTER to "not_an_exporter",
            OpenTelemetryEnvVars.LOG_LEVEL to "not_a_level",
        )
        val warnings = mutableListOf<EnvVarReadWarning>()
        toBehavior(env::get, warnings::add)

        assertEquals(
            setOf(
                AttributeLimitsEnvVars.ATTRIBUTE_COUNT_LIMIT,
                ResourceEnvVars.RESOURCE_ATTRIBUTES,
                SamplerEnvVars.SAMPLER,
                SpanExporterEnvVars.TRACES_EXPORTER,
                LogRecordExporterEnvVars.LOGS_EXPORTER,
                OpenTelemetryEnvVars.LOG_LEVEL,
            ),
            warnings.map { it.name }.toSet(),
        )
    }

    @Test
    fun `should warn and ignore unknown OTEL_LOG_LEVEL`() {
        val env = mapOf(OpenTelemetryEnvVars.LOG_LEVEL to "verbose")
        val warnings = mutableListOf<EnvVarReadWarning>()
        val behavior = toBehavior(env::get, warnings::add)

        assertNull(behavior.logLevel)
        assertEquals(
            listOf(EnvVarReadWarning(OpenTelemetryEnvVars.LOG_LEVEL, "Unknown value 'verbose'; ignoring")),
            warnings,
        )
    }

    @Test
    fun `should parse OTEL_LOG_LEVEL case-insensitively without warning`() {
        val env = mapOf(OpenTelemetryEnvVars.LOG_LEVEL to "warn")
        val warnings = mutableListOf<EnvVarReadWarning>()
        val behavior = toBehavior(env::get, warnings::add)

        assertEquals(SeverityLevel.WARN, behavior.logLevel)
        assertEquals(emptyList(), warnings)
    }

    private fun behaviorFrom(vars: Map<String, String>) =
        OpenTelemetryEnvVars(reportingEnvVarReader(getEnvVar = vars::get)).toBehavior()

    private fun toBehavior(
        getEnvVar: (String) -> String?,
        onWarning: (EnvVarReadWarning) -> Unit = {},
    ): OpenTelemetryBehavior =
        OpenTelemetryEnvVars(reportingEnvVarReader(getEnvVar, onWarning)).toBehavior()
}
