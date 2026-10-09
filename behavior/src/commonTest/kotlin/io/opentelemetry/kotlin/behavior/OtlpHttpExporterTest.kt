package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OtlpHttpExporterTest {
    @Test
    fun startsWithDefaultValues() {
        val exporter = OtlpHttpExporter("endpoint")
        assertEquals(OtlpExporter.DEFAULT_TIMEOUT, exporter.timeout)
        assertNull(exporter.headers)
    }
}
