package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class SpanProcessorBehaviorTest {

    @Test
    fun simpleExporterStartsUnset() {
        assertNull(SpanProcessorBehavior.Simple().exporter)
    }

    @Test
    fun batchExporterStartsUnset() {
        assertNull(SpanProcessorBehavior.Batch().exporter)
    }

    @Test
    fun simpleStaysUnsetWhenNeitherLayerConfiguredExporter() {
        assertNull(
            SpanProcessorBehavior.Simple()
                .mergeWith(SpanProcessorBehavior.Simple()).exporter,
        )
    }

    @Test
    fun batchStaysUnsetWhenNeitherLayerConfiguredExporter() {
        assertNull(
            SpanProcessorBehavior.Batch()
                .mergeWith(SpanProcessorBehavior.Batch()).exporter,
        )
    }

    @Test
    fun simpleAdoptsExporterFromWhicheverLayerSuppliedIt() {
        val exporter = SpanExporterBehavior.Console

        assertEquals(
            exporter,
            SpanProcessorBehavior.Simple()
                .mergeWith(SpanProcessorBehavior.Simple(exporter = exporter)).exporter,
        )
        assertEquals(
            exporter,
            SpanProcessorBehavior.Simple(exporter = exporter)
                .mergeWith(SpanProcessorBehavior.Simple()).exporter,
        )
    }

    @Test
    fun batchAdoptsExporterFromWhicheverLayerSuppliedIt() {
        val exporter = SpanExporterBehavior.Console

        assertEquals(
            exporter,
            SpanProcessorBehavior.Batch()
                .mergeWith(SpanProcessorBehavior.Batch(exporter = exporter)).exporter,
        )
        assertEquals(
            exporter,
            SpanProcessorBehavior.Batch(exporter = exporter)
                .mergeWith(SpanProcessorBehavior.Batch()).exporter,
        )
    }

    @Test
    fun mergingDifferentProcessorTypesUsesHigher() {
        val lower = SpanProcessorBehavior.Simple(exporter = SpanExporterBehavior.Console)
        val higher = SpanProcessorBehavior.Batch(exporter = SpanExporterBehavior.Console)

        val result = lower.mergeWith(higher)
        assertTrue(result is SpanProcessorBehavior.Batch)
    }

    @Test
    fun simpleWithoutExporterMergesWithSimpleWithExporter() {
        val exporter = SpanExporterBehavior.Console
        val result = SpanProcessorBehavior.Simple().mergeWith(SpanProcessorBehavior.Simple(exporter = exporter))
        assertEquals(exporter, result.exporter)
    }

    @Test
    fun batchWithoutExporterMergesWithBatchWithExporter() {
        val exporter = SpanExporterBehavior.Console
        val result = SpanProcessorBehavior.Batch().mergeWith(SpanProcessorBehavior.Batch(exporter = exporter))
        assertEquals(exporter, result.exporter)
    }

    @Test
    fun simpleWithOtlpExporterMergesWithSimpleWithConsoleExporter() {
        val otlp = SpanExporterBehavior.OtlpHttp(endpoint = "https://example.com")
        val console = SpanExporterBehavior.Console
        val result = SpanProcessorBehavior.Simple(exporter = otlp).mergeWith(SpanProcessorBehavior.Simple(exporter = console))
        assertEquals(otlp, result.exporter)
    }

    @Test
    fun simpleWithConsoleExporterMergesWithSimpleWithOtlpExporter() {
        val otlp = SpanExporterBehavior.OtlpHttp(endpoint = "https://example.com")
        val console = SpanExporterBehavior.Console
        val result = SpanProcessorBehavior.Simple(exporter = console).mergeWith(SpanProcessorBehavior.Simple(exporter = otlp))
        assertEquals(otlp, result.exporter)
    }

    @Test
    fun batchWithOtlpExporterMergesWithBatchWithConsoleExporter() {
        val otlp = SpanExporterBehavior.OtlpHttp(endpoint = "https://example.com")
        val console = SpanExporterBehavior.Console
        val result = SpanProcessorBehavior.Batch(exporter = otlp).mergeWith(SpanProcessorBehavior.Batch(exporter = console))
        assertEquals(otlp, result.exporter)
    }

    @Test
    fun batchWithConsoleExporterMergesWithBatchWithOtlpExporter() {
        val otlp = SpanExporterBehavior.OtlpHttp(endpoint = "https://example.com")
        val console = SpanExporterBehavior.Console
        val result = SpanProcessorBehavior.Batch(exporter = console).mergeWith(SpanProcessorBehavior.Batch(exporter = otlp))
        assertEquals(otlp, result.exporter)
    }
}
