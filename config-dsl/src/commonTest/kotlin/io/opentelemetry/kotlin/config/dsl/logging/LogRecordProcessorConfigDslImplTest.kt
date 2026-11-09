package io.opentelemetry.kotlin.config.dsl.logging

import io.opentelemetry.kotlin.behavior.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogRecordProcessorConfigDslImplTest {

    @Test
    fun startsUnset() {
        assertNull(LogRecordProcessorConfigDslImpl().toBehavior())
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
