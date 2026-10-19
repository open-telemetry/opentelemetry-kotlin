package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.config.schema.model.AlwaysOffSampler
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.IdGenerator
import io.opentelemetry.kotlin.config.schema.model.RandomIdGenerator
import io.opentelemetry.kotlin.config.schema.model.Sampler
import io.opentelemetry.kotlin.config.schema.model.SimpleSpanProcessor
import io.opentelemetry.kotlin.config.schema.model.SpanExporter
import io.opentelemetry.kotlin.config.schema.model.SpanLimits
import io.opentelemetry.kotlin.config.schema.model.SpanProcessor
import io.opentelemetry.kotlin.config.schema.model.TracerProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class TracerProviderMapperTest {

    @Test
    fun unsetIfNoMappedFields() {
        assertNull(TracerProvider(processors = emptyList()).toBehavior())
    }

    @Test
    fun mapsTracerProviderFields() {
        val provider = TracerProvider(
            processors = emptyList(),
            limits = SpanLimits(attributeCountLimit = 1),
            sampler = Sampler(alwaysOff = AlwaysOffSampler()),
            idGenerator = IdGenerator(random = RandomIdGenerator()),
        )
        assertEquals(
            TracerProviderBehavior(
                spanLimits = SpanLimitsBehavior(attributeCountLimit = 1),
                sampler = SamplerBehavior.AlwaysOff,
                idGenerator = IdGeneratorBehavior.Random,
            ),
            provider.toBehavior(),
        )
    }

    @Test
    fun anySingleMappedFieldSetsBehavior() {
        val providers = listOf(
            TracerProvider(processors = emptyList(), limits = SpanLimits()),
            TracerProvider(processors = emptyList(), sampler = Sampler(alwaysOff = AlwaysOffSampler())),
            TracerProvider(processors = emptyList(), idGenerator = IdGenerator(random = RandomIdGenerator())),
            TracerProvider(
                processors = listOf(
                    SpanProcessor(simple = SimpleSpanProcessor(exporter = SpanExporter(console = ConsoleExporter()))),
                ),
            ),
        )
        providers.forEach { assertNotNull(it.toBehavior()) }
    }
}
