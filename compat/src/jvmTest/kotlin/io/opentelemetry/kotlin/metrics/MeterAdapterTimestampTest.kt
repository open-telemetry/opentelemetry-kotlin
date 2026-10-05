package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaClock
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaMetricReader
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class MeterAdapterTimestampTest {

    private val sdkClock = FakeOtelJavaClock(start = 1_000_000)
    private val reader = FakeOtelJavaMetricReader()

    private val meter = MeterProviderAdapter(
        OtelJavaSdkMeterProvider.builder()
            .setClock(sdkClock)
            .registerMetricReader(reader)
            .build()
    ).getMeter("test")

    @Test
    fun `points are timed by the sdk clock`() {
        val counter = meter.createLongUpDownCounter("counter")
        sdkClock.advance(100)
        counter.add(1)
        sdkClock.advance(400)

        val point = reader.collect().single().longSumData.points.single()
        assertEquals(1_000_100, point.startEpochNanos)
        assertEquals(1_000_500, point.epochNanos)
    }
}
