package io.opentelemetry.kotlin.config.dsl.tracing

import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BatchSpanProcessorConfigDslImplTest {
    @Test
    fun startsUnset() {
        val dsl = BatchSpanProcessorConfigDslImpl()
        assertEquals(
            BatchSpanProcessorBehavior(),
            dsl.toBehavior()
        )
    }

    @Test
    fun mapsAllFields() {
        val dsl = BatchSpanProcessorConfigDslImpl().apply {
            scheduleDelay = 1
            exporterTimeout = 2
            maxQueueSize = 3
            maxExportBatchSize = 4
            exporter {
                http {
                    endpoint = "http://www.example.com"
                }
                console()
            }
        }
        assertEquals(
            BatchSpanProcessorBehavior(
                scheduleDelay = 1,
                exportTimeout = 2,
                maxQueueSize = 3,
                maxExportBatchSize = 4,
                exporter = SpanExporterBehavior(
                    http = OtlpHttpSpanExporterBehavior(
                        endpoint = "http://www.example.com"
                    ),
                    console = ConsoleExporterBehavior()
                )
            ),
            dsl.toBehavior()
        )
    }

    @Test
    fun invalidFieldValuesFallbackToDefault() {
        val dsl = BatchSpanProcessorConfigDslImpl().apply {
            scheduleDelay = -1
            exporterTimeout = -1
            maxQueueSize = 0
            maxExportBatchSize = 0
        }
        assertEquals(
            BatchSpanProcessorBehavior(),
            dsl.toBehavior()
        )
    }
}
