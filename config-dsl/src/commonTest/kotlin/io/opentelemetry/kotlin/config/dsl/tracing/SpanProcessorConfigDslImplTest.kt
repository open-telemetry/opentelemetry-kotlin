package io.opentelemetry.kotlin.config.dsl.tracing

import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SpanProcessorConfigDslImplTest {

    @Test
    fun startsUnset() {
        assertEquals(SpanProcessorBehavior(), SpanProcessorConfigDslImpl().toBehavior())
    }

    @Test
    fun mapsSimple() {
        val dsl = SpanProcessorConfigDslImpl().apply {
            simple {
                exporter {
                    console()
                }
            }
        }
        assertEquals(
            SpanProcessorBehavior(
                simple = SimpleSpanProcessorBehavior(
                    exporter = SpanExporterBehavior(
                        console = ConsoleExporterBehavior()
                    )
                )
            ),
            dsl.toBehavior(),
        )
    }

    @Test
    fun mapsBatch() {
        val dsl = SpanProcessorConfigDslImpl().apply {
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
        assertEquals(
            SpanProcessorBehavior(
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
            dsl.toBehavior(),
        )
    }

    @Test
    fun doesNotMapSimpleAndBatchTogether() {
        val dsl = SpanProcessorConfigDslImpl().apply {
            simple {
                exporter {
                    console()
                }
            }
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
        assertEquals(
            SpanProcessorBehavior(
                batch = BatchSpanProcessorBehavior(
                    exporter = SpanExporterBehavior(
                        console = ConsoleExporterBehavior()
                    ),
                    scheduleDelay = 1,
                    exportTimeout = 2,
                    maxQueueSize = 3,
                    maxExportBatchSize = 4
                ),
            ),
            dsl.toBehavior(),
        )
    }
}
