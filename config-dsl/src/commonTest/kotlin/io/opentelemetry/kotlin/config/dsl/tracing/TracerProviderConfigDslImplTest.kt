package io.opentelemetry.kotlin.config.dsl.tracing

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.tracing.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.tracing.SamplerBehavior
import io.opentelemetry.kotlin.behavior.tracing.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.tracing.SpanExporterBehavior
import io.opentelemetry.kotlin.behavior.tracing.SpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.tracing.TracerProviderBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TracerProviderConfigDslImplTest {

    @Test
    fun startsUnset() {
        assertEquals(TracerProviderBehavior(), TracerProviderConfigDslImpl().toBehavior())
    }

    @Test
    fun samplerCallSetsSampler() {
        val dsl = TracerProviderConfigDslImpl().apply {
            sampler { alwaysOn() }
        }
        assertEquals(
            TracerProviderBehavior(sampler = SamplerBehavior.AlwaysOn),
            dsl.toBehavior()
        )
    }

    @Test
    fun samplerMapsParentBased() {
        val dsl = TracerProviderConfigDslImpl().apply {
            sampler { parentBased(root = alwaysOff()) }
        }
        assertEquals(
            TracerProviderBehavior(
                sampler = SamplerBehavior.ParentBased(root = SamplerBehavior.AlwaysOff)
            ),
            dsl.toBehavior()
        )
    }

    @Test
    fun processorCallSetsProcessors() {
        val dsl = TracerProviderConfigDslImpl().apply {
            processor {
                simple {
                    exporter {
                        console()
                    }
                }
            }
            processor {
                batch {
                    exporter {
                        console()
                    }
                    scheduleDelay = 1
                    exporterTimeout = 2
                    maxQueueSize = 3
                    requestedQueueSize = 4
                }
            }
        }
        assertEquals(
            TracerProviderBehavior(
                processors = listOf(
                    SpanProcessorBehavior(
                        simple = SimpleSpanProcessorBehavior(
                            exporter = SpanExporterBehavior(
                                console = ConsoleExporterBehavior()
                            )
                        )
                    ),
                    SpanProcessorBehavior(
                        batch = BatchSpanProcessorBehavior(
                            scheduleDelay = 1,
                            exportTimeout = 2,
                            maxQueueSize = 3,
                            requestedQueueSize = 4,
                            exporter = SpanExporterBehavior(
                                console = ConsoleExporterBehavior()
                            )
                        )
                    ),
                ),
            ),
            dsl.toBehavior()
        )
    }
}
