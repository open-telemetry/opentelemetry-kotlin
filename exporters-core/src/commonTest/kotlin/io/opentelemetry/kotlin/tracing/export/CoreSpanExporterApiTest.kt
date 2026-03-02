package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.export.FakeTraceExportConfig
import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class CoreSpanExporterApiTest {

    private val config = FakeTraceExportConfig()

    @Test
    fun compositeSpanProcessorEmpty() {
        assertFailsWith<IllegalArgumentException> {
            config.compositeSpanProcessor()
        }
    }

    @Test
    fun compositeSpanExporterEmpty() {
        assertFailsWith<IllegalArgumentException> {
            config.compositeSpanExporter()
        }
    }
}
