package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleUpDownCounter
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleUpDownCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounter
import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import java.util.function.Consumer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class MeterAdapterUpDownCounterTest {

    @Test
    fun createLongUpDownCounterDelegatesAdd() {
        val javaMeter = RecordingJavaMeter()
        val counter = MeterAdapter(javaMeter).createLongUpDownCounter(
            name = "grocery.customers",
            unit = "{customer}",
            description = "customers in store",
        )
        assertEquals("grocery.customers", counter.name)
        assertEquals("{customer}", counter.unit)
        assertEquals("customers in store", counter.description)
        assertTrue(counter.enabled())
        counter.add(1)
        counter.add(-1)
        counter.add(0)
        counter.add(2) { setStringAttribute("account.type", "commercial") }
        assertEquals(
            listOf(
                1L to emptyMap(),
                -1L to emptyMap(),
                0L to emptyMap(),
                2L to mapOf("account.type" to "commercial"),
            ),
            javaMeter.longCounter.adds,
        )
    }

    @Test
    fun createLongUpDownCounterOmitsNullUnitAndDescription() {
        val javaMeter = RecordingJavaMeter()
        val counter = MeterAdapter(javaMeter).createLongUpDownCounter("grocery.customers")
        assertEquals("grocery.customers", counter.name)
        assertEquals(null, counter.unit)
        assertEquals(null, counter.description)
        assertEquals(null, javaMeter.unit)
        assertEquals(null, javaMeter.description)
        counter.add(1)
        assertEquals(listOf(1L to emptyMap()), javaMeter.longCounter.adds)
    }

    @Test
    fun createDoubleUpDownCounterDelegatesAdd() {
        val javaMeter = RecordingJavaMeter()
        val counter = MeterAdapter(javaMeter).createDoubleUpDownCounter(
            name = "queue.depth",
            unit = "{item}",
            description = "queue size",
        )
        assertEquals("queue.depth", counter.name)
        assertEquals("{item}", counter.unit)
        assertEquals("queue size", counter.description)
        assertTrue(counter.enabled())
        counter.add(1.0)
        counter.add(-1.0)
        counter.add(0.0)
        counter.add(2.5) { setStringAttribute("account.type", "residential") }
        assertEquals(
            listOf(
                1.0 to emptyMap(),
                -1.0 to emptyMap(),
                0.0 to emptyMap(),
                2.5 to mapOf("account.type" to "residential"),
            ),
            javaMeter.doubleCounter.adds,
        )
    }

    @Test
    fun createDoubleUpDownCounterOmitsNullUnitAndDescription() {
        val javaMeter = RecordingJavaMeter()
        val counter = MeterAdapter(javaMeter).createDoubleUpDownCounter("queue.depth")
        assertEquals("queue.depth", counter.name)
        assertEquals(null, counter.unit)
        assertEquals(null, counter.description)
        assertEquals(null, javaMeter.unit)
        assertEquals(null, javaMeter.description)
        counter.add(1.0)
        assertEquals(listOf(1.0 to emptyMap()), javaMeter.doubleCounter.adds)
    }

    private class RecordingJavaLongUpDownCounter : OtelJavaLongUpDownCounter {
        val adds = mutableListOf<Pair<Long, Map<String, Any>>>()

        override fun isEnabled(): Boolean = true

        override fun add(value: Long) {
            adds.add(value to emptyMap())
        }

        override fun add(value: Long, attributes: OtelJavaAttributes) {
            adds.add(value to attributes.asMap().mapKeys { it.key.key })
        }

        override fun add(value: Long, attributes: OtelJavaAttributes, context: OtelJavaContext) {
            add(value, attributes)
        }
    }

    private class RecordingJavaDoubleUpDownCounter : OtelJavaDoubleUpDownCounter {
        val adds = mutableListOf<Pair<Double, Map<String, Any>>>()

        override fun isEnabled(): Boolean = true

        override fun add(value: Double) {
            adds.add(value to emptyMap())
        }

        override fun add(value: Double, attributes: OtelJavaAttributes) {
            adds.add(value to attributes.asMap().mapKeys { it.key.key })
        }

        override fun add(value: Double, attributes: OtelJavaAttributes, context: OtelJavaContext) {
            add(value, attributes)
        }
    }

    private class RecordingJavaMeter : OtelJavaMeter by OtelJavaMeterProvider.noop().get("") {
        val longCounter = RecordingJavaLongUpDownCounter()
        val doubleCounter = RecordingJavaDoubleUpDownCounter()
        var unit: String? = null
        var description: String? = null

        override fun upDownCounterBuilder(name: String): OtelJavaLongUpDownCounterBuilder =
            object : OtelJavaLongUpDownCounterBuilder {
                override fun setUnit(unit: String) = apply {
                    this@RecordingJavaMeter.unit = unit
                }

                override fun setDescription(description: String) = apply {
                    this@RecordingJavaMeter.description = description
                }

                override fun ofDoubles(): OtelJavaDoubleUpDownCounterBuilder =
                    object : OtelJavaDoubleUpDownCounterBuilder {
                        override fun setUnit(unit: String) = apply {
                            this@RecordingJavaMeter.unit = unit
                        }

                        override fun setDescription(description: String) = apply {
                            this@RecordingJavaMeter.description = description
                        }

                        override fun build() = doubleCounter

                        override fun buildWithCallback(
                            callback: Consumer<OtelJavaObservableDoubleMeasurement>,
                        ) = OtelJavaMeterProvider.noop().get("").upDownCounterBuilder(name)
                            .ofDoubles().buildWithCallback(callback)
                    }

                override fun build() = longCounter

                override fun buildWithCallback(
                    callback: Consumer<OtelJavaObservableLongMeasurement>,
                ) = OtelJavaMeterProvider.noop().get("").upDownCounterBuilder(name)
                    .buildWithCallback(callback)
            }
    }
}
