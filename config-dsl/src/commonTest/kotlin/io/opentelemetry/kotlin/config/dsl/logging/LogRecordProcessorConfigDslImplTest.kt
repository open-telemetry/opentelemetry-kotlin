package io.opentelemetry.kotlin.config.dsl.logging

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.SimpleLogRecordProcessorBehavior
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogRecordProcessorConfigDslImplTest {

    @Test
    fun startsUnset() {
        val dsl = LogRecordProcessorConfigDslImpl()
        assertEquals(
            LogRecordProcessorBehavior(),
            dsl.toBehavior()
        )
    }

    @Test
    fun mapsSimple() {
        val dsl = LogRecordProcessorConfigDslImpl()
        dsl.simple {
            exporter { console() }
        }
        assertEquals(
            LogRecordProcessorBehavior(
                simple = SimpleLogRecordProcessorBehavior(
                    exporter = LogRecordExporterBehavior(console = ConsoleExporterBehavior()),
                ),
            ),
            dsl.toBehavior(),
        )
    }

    @Test
    fun mapsBatch() {
        val dsl = LogRecordProcessorConfigDslImpl()
        dsl.batch {
            scheduleDelay = 2_000L
            exporter {
                http { endpoint = "http://www.example.com:8080/logs" }
            }
        }
        assertEquals(
            LogRecordProcessorBehavior(
                batch = BatchLogRecordProcessorBehavior(
                    scheduleDelay = 2_000L,
                    exporter = LogRecordExporterBehavior(
                        http = OtlpHttpLogsExporterBehavior(endpoint = "http://www.example.com:8080/logs"),
                    ),
                ),
            ),
            dsl.toBehavior(),
        )
    }

    @Test
    fun batchReplacesEarlierSimple() {
        val dsl = LogRecordProcessorConfigDslImpl()
        dsl.simple {}
        dsl.batch {}
        assertEquals(
            LogRecordProcessorBehavior(batch = BatchLogRecordProcessorBehavior()),
            dsl.toBehavior(),
        )
    }

    @Test
    fun simpleReplacesEarlierBatch() {
        val dsl = LogRecordProcessorConfigDslImpl()
        dsl.batch {}
        dsl.simple {}
        assertEquals(
            LogRecordProcessorBehavior(simple = SimpleLogRecordProcessorBehavior()),
            dsl.toBehavior(),
        )
    }
}
