package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class OtelJavaUpDownCounterAdapterTest {

    @Test
    fun upDownCounterBuilderBuildRecordsOnKotlinMeter() {
        val meter = FakeMeter("test")
        val javaCounter = OtelJavaMeterAdapter(meter)
            .upDownCounterBuilder("grocery.customers")
            .setUnit("{customer}")
            .setDescription("customers in store")
            .build()

        assertTrue(javaCounter.isEnabled)
        javaCounter.add(1)
        javaCounter.add(-1, OtelJavaAttributes.builder().put("account.type", "commercial").build())

        val fake = meter.longUpDownCounters.single()
        assertEquals("grocery.customers", fake.name)
        assertEquals("{customer}", fake.unit)
        assertEquals("customers in store", fake.description)
        assertEquals(1L, fake.adds[0].first)
        assertEquals(-1L, fake.adds[1].first)
        assertEquals("commercial", fake.adds[1].second["account.type"])
    }

    @Test
    fun builderSettersReturnThis() {
        val builder = OtelJavaLongUpDownCounterBuilderAdapter(FakeMeter("test"), "n")
        assertSame(builder, builder.setUnit("1"))
        assertSame(builder, builder.setDescription("d"))
    }

    @Test
    fun isEnabledDelegates() {
        val fake = FakeLongUpDownCounter("n")
        val adapter = OtelJavaLongUpDownCounterAdapter(fake)
        assertTrue(adapter.isEnabled)
        fake.enabledResult = { false }
        assertFalse(adapter.isEnabled)
    }

    @Test
    fun addWithEmptyAttributesSkipsAttributeMap() {
        val fake = FakeLongUpDownCounter("n")
        OtelJavaLongUpDownCounterAdapter(fake).add(3, OtelJavaAttributes.empty())
        assertEquals(listOf(3L to emptyMap()), fake.adds)
    }

    @Test
    fun addWithContextDelegatesToAttributesOverload() {
        val fake = FakeLongUpDownCounter("n")
        OtelJavaLongUpDownCounterAdapter(fake).add(
            4,
            OtelJavaAttributes.builder().put("account.type", "commercial").build(),
            OtelJavaContext.root(),
        )
        assertEquals(4L, fake.adds.single().first)
        assertEquals("commercial", fake.adds.single().second["account.type"])
    }

    @Test
    fun ofDoublesUsesInjectedMeterProvider() {
        val javaMeterProvider = RecordingJavaMeterProvider()
        OtelJavaLongUpDownCounterBuilderAdapter(FakeMeter("test"), "n", javaMeterProvider)
            .ofDoubles()
            .build()
            .add(1.0)
        assertEquals(1, javaMeterProvider.getCount)
    }

    @Test
    fun buildWithCallbackUsesInjectedMeterProvider() {
        val javaMeterProvider = RecordingJavaMeterProvider()
        OtelJavaLongUpDownCounterBuilderAdapter(FakeMeter("test"), "n", javaMeterProvider)
            .buildWithCallback { }
            .close()
        assertEquals(1, javaMeterProvider.getCount)
    }

    private class RecordingJavaMeterProvider(
        private val delegate: OtelJavaMeterProvider = OtelJavaMeterProvider.noop(),
    ) : OtelJavaMeterProvider by delegate {
        var getCount = 0

        override fun get(instrumentationScopeName: String): OtelJavaMeter {
            getCount++
            return delegate.get(instrumentationScopeName)
        }
    }
}
