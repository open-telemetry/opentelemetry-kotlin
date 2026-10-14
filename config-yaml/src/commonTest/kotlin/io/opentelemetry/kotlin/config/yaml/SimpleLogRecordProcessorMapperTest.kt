package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.LogRecordExporter
import io.opentelemetry.kotlin.config.schema.model.SimpleLogRecordProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SimpleLogRecordProcessorMapperTest {

    @Test
    fun missingProcessorLeavesBehaviorUnset() {
        val processor: SimpleLogRecordProcessor? = null
        assertNull(processor.toBehavior())
    }

    @Test
    fun mapsConsoleExporter() {
        val processor = SimpleLogRecordProcessor(exporter = LogRecordExporter(console = ConsoleExporter()))
        assertEquals(
            SimpleLogRecordProcessorBehavior(
                exporter = LogRecordExporterBehavior(console = ConsoleExporterBehavior())
            ),
            processor.toBehavior(),
        )
    }

    @Test
    fun leavesExporterUnsetWhenNoneIsKnown() {
        val processor = SimpleLogRecordProcessor(exporter = LogRecordExporter())
        assertEquals(SimpleLogRecordProcessorBehavior(), processor.toBehavior())
    }
}
