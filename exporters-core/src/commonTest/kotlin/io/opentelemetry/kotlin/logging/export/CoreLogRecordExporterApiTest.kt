package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.export.FakeLogExportConfig
import kotlin.test.Test
import kotlin.test.assertSame

internal class CoreLogRecordExporterApiTest {

    private val config = FakeLogExportConfig()

    @Test
    fun compositeLogRecordProcessorEmptyReturnsNoop() {
        assertSame(NoopLogRecordProcessor, config.compositeLogRecordProcessor())
    }

    @Test
    fun compositeLogRecordExporterEmptyReturnsNoop() {
        assertSame(NoopLogRecordExporter, config.compositeLogRecordExporter())
    }
}
