package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class FloatCounterAdapterTest {

    private val meterProvider = OtelJavaSdkMeterProvider.builder().build()
    private lateinit var adapter: FloatCounterAdapter

    @BeforeTest
    fun setUp() {

    }

    @Test
    fun nullNameAndDescriptionTest() {
        adapter = FloatCounterAdapter("test_counter", null, null, meterProvider.get("test-scope"))
        assertNull(adapter.getUnit())
        assertNull(adapter.getDescription())
    }

    @Test
    fun nonNullNameAndDescriptionTest() {
        adapter = FloatCounterAdapter("test_counter", "units", "This is a test counter", meterProvider.get("test-scope"))
        assertNotNull(adapter.getUnit())
        assertNotNull(adapter.getDescription())
    }
}