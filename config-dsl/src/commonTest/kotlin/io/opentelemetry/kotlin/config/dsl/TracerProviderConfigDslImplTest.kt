package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
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
    fun samplerDoesNotDropProcessor() {
        val dsl = TracerProviderConfigDslImpl()
        dsl.export { error("behavior mapping does not run the export lambda") }
        dsl.sampler { alwaysOff() }

        assertEquals(
            TracerProviderBehavior(
                processor = SpanProcessorBehavior(),
                sampler = SamplerBehavior.AlwaysOff
            ),
            dsl.toBehavior()
        )
    }

    @Test
    fun exportCallSetsProcessor() {
        val dsl = TracerProviderConfigDslImpl()
        dsl.export { error("behavior mapping does not run the export lambda") }

        assertEquals(
            TracerProviderBehavior(
                processor = SpanProcessorBehavior()
            ),
            dsl.toBehavior(),
        )
    }

    @Test
    fun processorCallSetsSimple() {
        val dsl = TracerProviderConfigDslImpl().apply {
            processor {
                simple {
                    exporter {
                        console()
                    }
                }
            }
        }
        assertEquals(
            TracerProviderBehavior(
                processor = SpanProcessorBehavior(
                    simple = SimpleSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(
                            console = ConsoleExporterBehavior()
                        )
                    )
                ),
            ),
            dsl.toBehavior()
        )
    }

    @Test
    fun processorCallSetsBatch() {
        val dsl = TracerProviderConfigDslImpl().apply {
            processor {
                batch {
                    exporter {
                        console()
                    }
                    scheduleDelay = 1
                    exporterTimeout = 2
                    maxQueueSize = 3
                    maxExportBatchSize = 4
                }
            }
        }

        assertEquals(
            TracerProviderBehavior(
                processor = SpanProcessorBehavior(
                    batch = BatchSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(
                            console = ConsoleExporterBehavior()
                        ),
                        scheduleDelay = 1,
                        exportTimeout = 2,
                        maxQueueSize = 3,
                        maxExportBatchSize = 4
                    )
                ),
            ),
            dsl.toBehavior()
        )
    }

    @Test
    fun exportKeepsProcessorAlreadyChosen() {
        val dsl = TracerProviderConfigDslImpl().apply {
            processor {
                simple {
                    exporter {
                        console()
                    }
                }
            }
        }
        dsl.export { error("behavior mapping does not run the export lambda") }

        assertEquals(
            TracerProviderBehavior(
                processor = SpanProcessorBehavior(
                    simple = SimpleSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(
                            console = ConsoleExporterBehavior()
                        )
                    )
                ),
            ),
            dsl.toBehavior()
        )
    }

    @Test
    fun processorDoesNotDropSampler() {
        val dsl = TracerProviderConfigDslImpl().apply {
            sampler { alwaysOff() }
            processor {
                simple {
                    exporter {
                        console()
                    }
                }
            }
        }

        assertEquals(
            TracerProviderBehavior(
                processor = SpanProcessorBehavior(
                    simple = SimpleSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(
                            console = ConsoleExporterBehavior()
                        )
                    )
                ),
                sampler = SamplerBehavior.AlwaysOff
            ),
            dsl.toBehavior()
        )
    }
}
