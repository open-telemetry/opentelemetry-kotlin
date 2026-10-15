package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.ResourceBehavior
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.config.schema.model.AlwaysOffSampler
import io.opentelemetry.kotlin.config.schema.model.IdGenerator
import io.opentelemetry.kotlin.config.schema.model.OpenTelemetryConfiguration
import io.opentelemetry.kotlin.config.schema.model.RandomIdGenerator
import io.opentelemetry.kotlin.config.schema.model.Sampler
import io.opentelemetry.kotlin.config.schema.model.SpanLimits
import io.opentelemetry.kotlin.config.schema.model.TracerProvider
import io.opentelemetry.kotlin.framework.loadTestFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OpenTelemetryConfigurationMapperTest {

    @Test
    fun mapsEverySectionOfTheGoldenConfigFile() {
        val config = OpenTelemetryConfigurationParser().parse(loadTestFixture(GOLDEN_FILE))

        val expected = OpenTelemetryBehavior(
            attributeLimits = AttributeLimitsBehavior(
                attributeCountLimit = 128,
                attributeValueLengthLimit = 4096,
            ),
            resource = ResourceBehavior(
                schemaUrl = "https://opentelemetry.io/schemas/1.37.0",
                attributes = mapOf(
                    "service.namespace" to "demo",
                    "service.version" to "1.0.0",
                    "service.name" to "unknown_service",
                ),
            ),
            tracerProvider = TracerProviderBehavior(
                spanLimits = SpanLimitsBehavior(
                    attributeCountLimit = 128,
                    eventCountLimit = 64,
                ),
            ),
            loggerProvider = LoggerProviderBehavior(
                logLimits = LogLimitsBehavior(
                    attributeCountLimit = 64,
                    attributeValueLengthLimit = 256,
                ),
            ),
        )
        assertEquals(expected, config.toBehavior())
    }

    @Test
    fun leavesOmittedSectionsUnset() {
        val config = OpenTelemetryConfiguration(fileFormat = FILE_FORMAT)
        assertEquals(OpenTelemetryBehavior(), config.toBehavior())
    }

    @Test
    fun leavesLimitsTheSpecDisallowsUnset() {
        val behavior = OpenTelemetryConfiguration(
            fileFormat = FILE_FORMAT,
            tracerProvider = TracerProvider(
                processors = emptyList(),
                limits = SpanLimits(attributeCountLimit = -1),
            ),
        ).toBehavior()

        assertEquals(SpanLimitsBehavior(), behavior.tracerProvider?.spanLimits)
        assertNull(behavior.tracerProvider?.processor)
    }

    @Test
    fun mapsTracerProviderSampler() {
        val config = OpenTelemetryConfiguration(
            fileFormat = FILE_FORMAT,
            tracerProvider = TracerProvider(
                processors = emptyList(),
                sampler = Sampler(alwaysOff = AlwaysOffSampler()),
            )
        )
        assertEquals(
            TracerProviderBehavior(sampler = SamplerBehavior.AlwaysOff),
            config.toBehavior().tracerProvider
        )
    }

    @Test
    fun leavesOmittedSamplerUnset() {
        val config = OpenTelemetryConfiguration(
            fileFormat = FILE_FORMAT,
            tracerProvider = TracerProvider(processors = emptyList()),
        )
        assertEquals(null, config.toBehavior().tracerProvider?.sampler)
    }

    @Test
    fun mapsTracerProviderIdGenerator() {
        val config = OpenTelemetryConfiguration(
            fileFormat = FILE_FORMAT,
            tracerProvider = TracerProvider(
                processors = emptyList(),
                idGenerator = IdGenerator(random = RandomIdGenerator()),
            ),
        )

        assertEquals(
            IdGeneratorBehavior.Random,
            config.toBehavior().tracerProvider?.idGenerator,
        )
    }

    private companion object {
        const val GOLDEN_FILE = "minimal_config.yaml"
        const val FILE_FORMAT = "1.0"
    }
}
