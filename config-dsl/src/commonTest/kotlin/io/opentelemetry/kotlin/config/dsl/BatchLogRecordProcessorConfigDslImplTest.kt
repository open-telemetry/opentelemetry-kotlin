package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

class BatchLogRecordProcessorConfigDslImplTest {
    @Test
    fun startsUnset() {
        val dsl = BatchLogRecordProcessorConfigDslImpl()
        assertEquals(
            BatchLogRecordProcessorBehavior(),
            dsl.toBehavior()
        )
    }

    @Test
    fun mapsAllFields() {
        val dsl = BatchLogRecordProcessorConfigDslImpl().apply {
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
            BatchLogRecordProcessorBehavior(
                scheduleDelay = 1,
                exportTimeout = 2,
                maxQueueSize = 3,
                maxExportBatchSize = 4,
                exporter = LogRecordExporterBehavior(
                    http = OtlpHttpLogsExporterBehavior(
                        endpoint = "http://www.example.com"
                    ),
                    console = ConsoleExporterBehavior()
                )
            ),
            dsl.toBehavior()
        )
    }
}
