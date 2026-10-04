package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OtlpHttpSpanExporterBehaviorTest {
    @Test
    fun startsWithDefaultValues() {
        val httpExporter = OtlpHttpSpanExporterBehavior()
        assertEquals(OtlpHttpSpanExporterBehavior.DEFAULT_ENDPOINT, httpExporter.endpoint)
        assertEquals(OtlpHttpExporterBehavior.DEFAULT_TIMEOUT, httpExporter.timeout)
        assertNull(httpExporter.headers)
    }
}
