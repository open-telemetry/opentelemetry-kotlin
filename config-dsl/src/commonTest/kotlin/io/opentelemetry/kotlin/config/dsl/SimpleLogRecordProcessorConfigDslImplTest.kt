package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleLogRecordProcessorConfigDslImplTest {
    @Test
    fun startsUnset() {
        val dsl = SimpleLogRecordProcessorConfigDslImpl()
        assertEquals(
            SimpleLogRecordProcessorBehavior(),
            dsl.toBehavior()
        )
    }

    @Test
    fun mapsAllFields() {
        val dsl = SimpleLogRecordProcessorConfigDslImpl().apply {
            exporter {
                console()
            }
        }
        assertEquals(
            SimpleLogRecordProcessorBehavior(
                exporter = LogRecordExporterBehavior(
                    console = ConsoleExporterBehavior()
                )
            ),
            dsl.toBehavior()
        )
    }
}
