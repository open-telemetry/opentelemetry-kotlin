package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class OtelJavaMeterAdapterBuildObserverTest {

    @Test
    fun counterBuildersReturnRecordingObservers() {
        val meter = FakeMeter("test")
        val adapter = OtelJavaMeterAdapter(meter)
        val longObserver = adapter.counterBuilder("jobs")
            .setUnit("{job}")
            .setDescription("Completed jobs")
            .buildObserver()
        val doubleObserver = adapter.counterBuilder("cpu")
            .ofDoubles()
            .setUnit("s")
            .setDescription("CPU time")
            .buildObserver()

        longObserver.record(3, OtelJavaAttributes.builder().put("worker", "first").build())
        doubleObserver.record(1.5)

        assertEquals("jobs", meter.observableLongCounters.single().name)
        assertEquals("{job}", meter.observableLongCounters.single().unit)
        assertEquals("Completed jobs", meter.observableLongCounters.single().description)
        assertEquals(listOf(3L to mapOf<String, Any>("worker" to "first")), meter.observableLongCounters.single().measurement.adds)
        assertEquals("cpu", meter.observableDoubleCounters.single().name)
        assertEquals("s", meter.observableDoubleCounters.single().unit)
        assertEquals("CPU time", meter.observableDoubleCounters.single().description)
        assertEquals(listOf(1.5 to emptyMap()), meter.observableDoubleCounters.single().measurement.adds)
        assertTrue(meter.observableLongCounters.single().callbacks.isEmpty())
        assertTrue(meter.observableDoubleCounters.single().callbacks.isEmpty())
    }

    @Test
    fun upDownCounterBuildersReturnRecordingObservers() {
        val meter = FakeMeter("test")
        val adapter = OtelJavaMeterAdapter(meter)
        val longObserver = adapter.upDownCounterBuilder("queue")
            .setUnit("{item}")
            .buildObserver()
        val doubleObserver = adapter.upDownCounterBuilder("load")
            .ofDoubles()
            .setDescription("Current load")
            .buildObserver()

        longObserver.record(-2)
        doubleObserver.record(-0.5)

        assertEquals("{item}", meter.observableLongUpDownCounters.single().unit)
        assertEquals(listOf(-2L to emptyMap()), meter.observableLongUpDownCounters.single().measurement.adds)
        assertEquals("Current load", meter.observableDoubleUpDownCounters.single().description)
        assertEquals(listOf(-0.5 to emptyMap()), meter.observableDoubleUpDownCounters.single().measurement.adds)
        assertTrue(meter.observableLongUpDownCounters.single().callbacks.isEmpty())
        assertTrue(meter.observableDoubleUpDownCounters.single().callbacks.isEmpty())
    }

    @Test
    fun gaugeBuildersReturnRecordingObservers() {
        val meter = FakeMeter("test")
        val adapter = OtelJavaMeterAdapter(meter)
        val longObserver = adapter.gaugeBuilder("queue.depth")
            .ofLongs()
            .setUnit("{item}")
            .buildObserver()
        val doubleObserver = adapter.gaugeBuilder("temperature")
            .setUnit("Cel")
            .setDescription("Current temperature")
            .buildObserver()

        longObserver.record(4)
        doubleObserver.record(21.5)

        assertEquals("{item}", meter.observableLongGauges.single().unit)
        assertEquals(listOf(4L to emptyMap()), meter.observableLongGauges.single().measurement.adds)
        assertEquals("Cel", meter.observableDoubleGauges.single().unit)
        assertEquals("Current temperature", meter.observableDoubleGauges.single().description)
        assertEquals(listOf(21.5 to emptyMap()), meter.observableDoubleGauges.single().measurement.adds)
        assertTrue(meter.observableLongGauges.single().callbacks.isEmpty())
        assertTrue(meter.observableDoubleGauges.single().callbacks.isEmpty())
    }
}
