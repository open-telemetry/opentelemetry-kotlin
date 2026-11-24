package io.opentelemetry.kotlin.behavior.metrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PullMetricReaderBehaviorTest {

    @Test
    fun exporterStartsUnset() {
        assertNull(PullMetricReaderBehavior().exporter)
    }

    @Test
    fun adoptsExporterFromWhicheverLayerSuppliedIt() {
        val exporter = PullMetricExporterBehavior(
            prometheus = PrometheusMetricExporterBehavior(host = "0.0.0.0", port = 9464),
        )

        assertEquals(
            exporter,
            PullMetricReaderBehavior().mergeWith(PullMetricReaderBehavior(exporter = exporter)).exporter,
        )
        assertEquals(
            exporter,
            PullMetricReaderBehavior(exporter = exporter).mergeWith(PullMetricReaderBehavior()).exporter,
        )
    }

    @Test
    fun mergesPrometheusFields() {
        val lower = PullMetricReaderBehavior(
            exporter = PullMetricExporterBehavior(
                prometheus = PrometheusMetricExporterBehavior(host = "localhost", port = 9464),
            ),
        )
        val higher = PullMetricReaderBehavior(
            exporter = PullMetricExporterBehavior(
                prometheus = PrometheusMetricExporterBehavior(port = 9090),
            ),
        )

        val merged = lower.mergeWith(higher)

        assertEquals("localhost", merged.exporter?.prometheus?.host)
        assertEquals(9090, merged.exporter?.prometheus?.port)
    }
}
