package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogRecordExporterBehaviorTest {

    @Test
    fun mergesExportersFieldByFieldAcrossLayers() {
        val console = ConsoleExporterBehavior()
        val http = OtlpHttpLogsExporterBehavior(endpoint = "https://example.com")

        val merged = LogRecordExporterBehavior(console = console)
            .mergeWith(LogRecordExporterBehavior(http = http))

        assertEquals(LogRecordExporterBehavior(console = console, http = http), merged)
    }

    @Test
    fun higherLayerWinsPerExporter() {
        val low = OtlpHttpLogsExporterBehavior(endpoint = "https://low.example.com")
        val high = OtlpHttpLogsExporterBehavior(endpoint = "https://high.example.com")

        val merged = LogRecordExporterBehavior(http = low).mergeWith(LogRecordExporterBehavior(http = high))

        assertEquals("https://high.example.com", merged.http?.endpoint)
    }
}
