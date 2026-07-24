package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class FloatCounterAdapterTest {

    private val meterProvider = OtelJavaSdkMeterProvider.builder().build()
    private lateinit var adapter: FloatCounterAdapter

    @Test
    fun nullNameAndDescriptionTest() {
        adapter = FloatCounterAdapter("test_counter", null, null, meterProvider.get("test-scope"))
        assertNull(adapter.unit)
        assertNull(adapter.description)
    }

    @Test
    fun nonNullNameAndDescriptionTest() {
        adapter = FloatCounterAdapter("test_counter", "units", "This is a test counter", meterProvider.get("test-scope"))

        assertNotNull(adapter.unit)
        assertNotNull(adapter.description)
    }
}
