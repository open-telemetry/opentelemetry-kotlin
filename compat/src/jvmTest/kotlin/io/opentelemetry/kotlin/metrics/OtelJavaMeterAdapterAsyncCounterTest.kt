package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class OtelJavaMeterAdapterAsyncCounterTest {

    @Test
    fun doubleCounterBuilderRecordsCallbackAndClosesIt() {
        val meter = FakeMeter("test")
        val longBuilder = OtelJavaMeterAdapter(meter).counterBuilder("process.cpu.time")
        assertSame(longBuilder, longBuilder.setUnit("ms"))
        assertSame(longBuilder, longBuilder.setDescription("CPU time"))
        val doubleBuilder = longBuilder.ofDoubles()
        assertSame(doubleBuilder, doubleBuilder.setUnit("s"))
        assertSame(doubleBuilder, doubleBuilder.setDescription("CPU time (double)"))
        val counter = doubleBuilder.buildWithCallback { measurement ->
            measurement.record(1.5, OtelJavaAttributes.builder().put("process", "worker").build())
        }
        val observableCounter = meter.observableDoubleCounters.single()
        val measurement = FakeObservableDoubleMeasurement()

        observableCounter.invokeCallbacks(measurement)

        assertEquals("process.cpu.time", observableCounter.name)
        assertEquals("s", observableCounter.unit)
        assertEquals("CPU time (double)", observableCounter.description)
        assertEquals(listOf(1.5 to mapOf<String, Any>("process" to "worker")), measurement.adds)

        counter.close()
        assertTrue(observableCounter.callbacks.isEmpty())
    }

    @Test
    fun longCounterBuilderRecordsCallbackAndClosesIt() {
        val meter = FakeMeter("test")
        val counter = OtelJavaMeterAdapter(meter)
            .counterBuilder("process.page.faults")
            .setUnit("{fault}")
            .setDescription("Page faults")
            .buildWithCallback { measurement ->
                measurement.record(4L, OtelJavaAttributes.builder().put("process", "worker").build())
            }
        val observableCounter = meter.observableLongCounters.single()
        val measurement = FakeObservableLongMeasurement()

        observableCounter.invokeCallbacks(measurement)

        assertEquals("process.page.faults", observableCounter.name)
        assertEquals("{fault}", observableCounter.unit)
        assertEquals("Page faults", observableCounter.description)
        assertEquals(listOf(4L to mapOf<String, Any>("process" to "worker")), measurement.adds)

        counter.close()
        assertTrue(observableCounter.callbacks.isEmpty())
    }

    @Test
    fun synchronousCounterBuildersRemainNoop() {
        val meter = FakeMeter("test")
        val javaMeter = OtelJavaMeterAdapter(meter)

        assertFalse(javaMeter.counterBuilder("long").build().isEnabled)
        assertFalse(javaMeter.counterBuilder("double").ofDoubles().build().isEnabled)
        assertTrue(meter.observableDoubleCounters.isEmpty())
    }
}
