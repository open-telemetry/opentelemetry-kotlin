package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleUpDownCounter
import io.opentelemetry.kotlin.aliases.OtelJavaLongCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounter
import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class MeterAdapterErrorHandlingTest {

    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun testThrowingAttributesDoesNotEscapeAdd() {
        val meter = MeterAdapter(OtelJavaSdkMeterProvider.builder().build().get("test"), errorHandler)

        meter.createLongUpDownCounter("customers").add(1) { error("boom") }
        meter.createDoubleUpDownCounter("queue.depth").add(1.5) { error("boom") }

        assertUserErrors(listOf("LongUpDownCounter.add failed", "DoubleUpDownCounter.add failed"))
    }

    @Test
    fun testThrowingDelegateDoesNotEscapeCounter() {
        val longCounter = LongUpDownCounterAdapter(ThrowingJavaLongCounter(), "customers", null, null, errorHandler)
        val doubleCounter =
            DoubleUpDownCounterAdapter(ThrowingJavaDoubleCounter(), "queue.depth", null, null, errorHandler)

        longCounter.add(1)
        doubleCounter.add(1.5)
        assertFalse(longCounter.enabled())
        assertFalse(doubleCounter.enabled())

        assertSdkErrors(
            listOf(
                "LongUpDownCounter.add failed",
                "DoubleUpDownCounter.add failed",
                "LongUpDownCounter.enabled failed",
                "DoubleUpDownCounter.enabled failed",
            ),
        )
    }

    @Test
    fun testThrowingBuilderReturnsNoopInstrument() {
        val meter = MeterAdapter(ThrowingJavaMeter(), errorHandler)
        val longCounter = meter.createLongUpDownCounter("long.counter")
        val doubleCounter = meter.createDoubleUpDownCounter("double.counter")

        assertFalse(longCounter.enabled())
        assertFalse(doubleCounter.enabled())
        longCounter.add(1)
        doubleCounter.add(1.0)
        assertSdkErrors(listOf("Meter.createLongUpDownCounter failed", "Meter.createDoubleUpDownCounter failed"))
    }

    @OptIn(ExperimentalApi::class)
    @Test
    fun testThrowingObservableLongCounterBuilderReturnsNoopRegistration() {
        val meter = MeterAdapter(ThrowingJavaMeter(), errorHandler)
        val counter = meter.createObservableLongCounter("long.counter")

        val registration = counter.registerCallback { it.record(1L) }
        assertSame(NoopObservableCounterRegistration, registration)
        registration.close()
        counter.close()
        assertSdkErrors(listOf("ObservableLongCounter.registerCallback failed"))
    }

    @OptIn(ExperimentalApi::class)
    @Test
    fun testThrowingObservableLongUpDownCounterBuilderReturnsNoopRegistration() {
        val meter = MeterAdapter(ThrowingJavaMeter(), errorHandler)
        val counter = meter.createObservableLongUpDownCounter("long.up_down.counter")

        val registration = counter.registerCallback { it.record(-1L) }
        assertSame(NoopObservableCounterRegistration, registration)
        registration.close()
        counter.close()
        assertSdkErrors(listOf("ObservableLongUpDownCounter.registerCallback failed"))
    }

    @Test
    fun testThrowingAttributesDoesNotEscapeRecord() {
        val longMeasurement = ThrowingJavaLongMeasurement(throws = false)
        val doubleMeasurement = ThrowingJavaDoubleMeasurement(throws = false)

        ObservableLongMeasurementAdapter(longMeasurement, errorHandler).record(1) { error("boom") }
        ObservableDoubleMeasurementAdapter(doubleMeasurement, errorHandler).record(1.5) { error("boom") }

        assertUserErrors(listOf("ObservableLongMeasurement.record failed", "ObservableDoubleMeasurement.record failed"))
        assertEquals(0, longMeasurement.records)
        assertEquals(0, doubleMeasurement.records)
    }

    @Test
    fun testThrowingDelegateDoesNotEscapeRecord() {
        val longAdapter = ObservableLongMeasurementAdapter(ThrowingJavaLongMeasurement(throws = true), errorHandler)
        val doubleAdapter =
            ObservableDoubleMeasurementAdapter(ThrowingJavaDoubleMeasurement(throws = true), errorHandler)

        longAdapter.record(1)
        longAdapter.record(1) { setStringAttribute("color", "red") }
        doubleAdapter.record(1.5)
        doubleAdapter.record(1.5) { setStringAttribute("color", "blue") }

        assertSdkErrors(
            listOf(
                "ObservableLongMeasurement.record failed",
                "ObservableLongMeasurement.record failed",
                "ObservableDoubleMeasurement.record failed",
                "ObservableDoubleMeasurement.record failed",
            ),
        )
    }

    private fun assertUserErrors(messages: List<String>) {
        assertEquals(messages, errorHandler.userCodeErrors.map { it.message })
        assertTrue(errorHandler.userCodeErrors.all { it.cause.message == "boom" })
        assertTrue(errorHandler.userCodeErrors.all { it.severity == SdkErrorSeverity.WARNING })
        assertEquals(0, errorHandler.sdkCodeErrors.size)
    }

    private fun assertSdkErrors(messages: List<String>) {
        assertEquals(messages, errorHandler.sdkCodeErrors.map { it.message })
        assertTrue(errorHandler.sdkCodeErrors.all { it.cause.message == "boom" })
        assertTrue(errorHandler.sdkCodeErrors.all { it.severity == SdkErrorSeverity.WARNING })
        assertEquals(0, errorHandler.userCodeErrors.size)
    }

    private class ThrowingJavaMeter : OtelJavaMeter by OtelJavaMeterProvider.noop().get("") {
        override fun upDownCounterBuilder(name: String): OtelJavaLongUpDownCounterBuilder = error("boom")
        override fun counterBuilder(name: String): OtelJavaLongCounterBuilder = error("boom")
    }

    private class ThrowingJavaLongCounter : OtelJavaLongUpDownCounter {
        override fun isEnabled(): Boolean = error("boom")
        override fun add(value: Long): Unit = error("boom")
        override fun add(value: Long, attributes: OtelJavaAttributes): Unit = error("boom")
        override fun add(value: Long, attributes: OtelJavaAttributes, context: OtelJavaContext): Unit = error("boom")
    }

    private class ThrowingJavaDoubleCounter : OtelJavaDoubleUpDownCounter {
        override fun isEnabled(): Boolean = error("boom")
        override fun add(value: Double): Unit = error("boom")
        override fun add(value: Double, attributes: OtelJavaAttributes): Unit = error("boom")
        override fun add(value: Double, attributes: OtelJavaAttributes, context: OtelJavaContext): Unit = error("boom")
    }

    private class ThrowingJavaLongMeasurement(private val throws: Boolean) : OtelJavaObservableLongMeasurement {
        var records = 0

        override fun record(value: Long) = store()
        override fun record(value: Long, attributes: OtelJavaAttributes) = store()

        private fun store() {
            if (throws) {
                error("boom")
            }
            records++
        }
    }

    private class ThrowingJavaDoubleMeasurement(private val throws: Boolean) : OtelJavaObservableDoubleMeasurement {
        var records = 0

        override fun record(value: Double) = store()
        override fun record(value: Double, attributes: OtelJavaAttributes) = store()

        private fun store() {
            if (throws) {
                error("boom")
            }
            records++
        }
    }
}
