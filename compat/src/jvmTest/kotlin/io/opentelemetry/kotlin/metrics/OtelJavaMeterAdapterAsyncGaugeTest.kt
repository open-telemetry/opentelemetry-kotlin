package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class OtelJavaMeterAdapterAsyncGaugeTest {

    @Test
    fun doubleGaugeBuilderRecordsCallbackAndClosesIt() {
        val meter = FakeMeter("test")
        val builder = OtelJavaMeterAdapter(meter).gaugeBuilder("temperature")
        assertSame(builder, builder.setUnit("Cel"))
        assertSame(builder, builder.setDescription("Current temperature"))
        val gauge = builder.buildWithCallback { measurement ->
            measurement.record(21.5, OtelJavaAttributes.builder().put("room", "server").build())
        }
        val observableGauge = meter.observableDoubleGauges.single()
        val measurement = FakeObservableDoubleMeasurement()

        observableGauge.invokeCallbacks(measurement)

        assertEquals("temperature", observableGauge.name)
        assertEquals("Cel", observableGauge.unit)
        assertEquals("Current temperature", observableGauge.description)
        assertEquals(listOf(21.5 to mapOf<String, Any>("room" to "server")), measurement.adds)

        gauge.close()
        assertTrue(observableGauge.callbacks.isEmpty())
    }

    @Test
    fun longGaugeBuilderRecordsCallbackAndClosesIt() {
        val meter = FakeMeter("test")
        val gauge = OtelJavaMeterAdapter(meter)
            .gaugeBuilder("queue.depth")
            .setUnit("{item}")
            .setDescription("Current queue depth")
            .ofLongs()
            .buildWithCallback { measurement ->
                measurement.record(4L, OtelJavaAttributes.builder().put("queue", "primary").build())
            }
        val observableGauge = meter.observableLongGauges.single()
        val measurement = FakeObservableLongMeasurement()

        observableGauge.invokeCallbacks(measurement)

        assertEquals("queue.depth", observableGauge.name)
        assertEquals("{item}", observableGauge.unit)
        assertEquals("Current queue depth", observableGauge.description)
        assertEquals(listOf(4L to mapOf<String, Any>("queue" to "primary")), measurement.adds)

        gauge.close()
        assertTrue(observableGauge.callbacks.isEmpty())
    }

    @Test
    fun longGaugeBuilderCanSetMetadataAfterConvertingToLong() {
        val meter = FakeMeter("test")
        val gauge = OtelJavaMeterAdapter(meter)
            .gaugeBuilder("queue.depth")
            .ofLongs()
            .setUnit("{item}")
            .setDescription("Current queue depth")
            .buildWithCallback { measurement -> measurement.record(1L) }
        val observableGauge = meter.observableLongGauges.single()

        assertEquals("{item}", observableGauge.unit)
        assertEquals("Current queue depth", observableGauge.description)

        gauge.close()
        assertTrue(observableGauge.callbacks.isEmpty())
    }

    @Test
    fun ofLongsBuildsSynchronousLongGauge() {
        val meter = FakeMeter("test")
        val gauge = OtelJavaMeterAdapter(meter)
            .gaugeBuilder("queue.depth")
            .setUnit("{item}")
            .setDescription("Current queue depth")
            .ofLongs()
            .build()

        assertTrue(gauge.isEnabled)
        assertEquals("{item}", meter.longGauges.single().unit)
        assertEquals("Current queue depth", meter.longGauges.single().description)
        assertTrue(meter.observableLongGauges.isEmpty())
    }
}
