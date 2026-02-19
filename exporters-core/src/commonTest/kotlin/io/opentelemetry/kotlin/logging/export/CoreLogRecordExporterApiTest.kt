package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.FakeLogExportConfig
import kotlin.test.Test
import kotlin.test.assertFailsWith

@OptIn(ExperimentalApi::class)
internal class CoreLogRecordExporterApiTest {

    private val config = FakeLogExportConfig()

    @Test
    fun compositeLogRecordProcessorEmpty() {
        assertFailsWith<IllegalArgumentException> {
            config.compositeLogRecordProcessor()
        }
    }

    @Test
    fun compositeLogRecordExporterEmpty() {
        assertFailsWith<IllegalArgumentException> {
            config.compositeLogRecordExporter()
        }
    }
}
