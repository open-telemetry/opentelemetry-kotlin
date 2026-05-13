package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.export.FakeTraceExportConfig
import kotlin.test.Test
import kotlin.test.assertSame

internal class CoreSpanExporterApiTest {

    private val config = FakeTraceExportConfig()

    @Test
    fun compositeSpanProcessorEmptyReturnsNoop() {
        assertSame(NoopSpanProcessor, config.compositeSpanProcessor())
    }

    @Test
    fun compositeSpanExporterEmptyReturnsNoop() {
        assertSame(NoopSpanExporter, config.compositeSpanExporter())
    }
}
