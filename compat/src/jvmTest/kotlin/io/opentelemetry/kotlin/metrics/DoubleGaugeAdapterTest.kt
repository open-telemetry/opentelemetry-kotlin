package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class DoubleGaugeAdapterTest {

    @Test
    fun javaGaugeAdapterRecordsAttributes() {
        val fake = RecordingDoubleGauge("temperature")
        val adapter = OtelJavaDoubleGaugeAdapter(fake)

        adapter.set(21.5, OtelJavaAttributes.builder().put("room", "server").build())

        assertEquals(
            listOf(21.5 to mapOf<String, Any>("room" to "server")),
            fake.records,
        )
    }

    @Test
    fun javaGaugeAdapterDelegatesValueAndContext() {
        val fake = RecordingDoubleGauge("temperature")
        val adapter = OtelJavaDoubleGaugeAdapter(fake)

        adapter.set(21.5)
        adapter.set(22.0, OtelJavaAttributes.empty())
        adapter.set(23.0, OtelJavaAttributes.empty(), io.opentelemetry.kotlin.aliases.OtelJavaContext.root())

        assertTrue(adapter.isEnabled)
        assertEquals(listOf(21.5 to emptyMap(), 22.0 to emptyMap(), 23.0 to emptyMap()), fake.records)
    }

    @Test
    fun kotlinGaugeAdapterBuildsAndForwardsMetadata() {
        val meter = FakeMeter("test")
        val builder = OtelJavaMeterAdapter(meter).gaugeBuilder("temperature")

        assertSame(builder, builder.setUnit("Cel"))
        assertSame(builder, builder.setDescription("Current temperature"))
        val gauge = builder.build()

        assertTrue(gauge.isEnabled)
        assertEquals("Cel", meter.doubleGauges.single().unit)
        assertEquals("Current temperature", meter.doubleGauges.single().description)
    }

    @Test
    fun kotlinGaugeAdapterReportsUserAndSdkErrors() {
        val errors = FakeSdkErrorHandler()
        val throwing = ThrowingJavaDoubleGauge()
        val adapter = DoubleGaugeAdapter(throwing, "temperature", null, null, errors)

        adapter.record(1.0) { error("user") }
        adapter.record(1.0)
        assertFalse(adapter.enabled())

        assertEquals(
            listOf("DoubleGauge.record failed"),
            errors.userCodeErrors.map { it.message },
        )
        assertEquals(
            listOf("DoubleGauge.record failed", "DoubleGauge.enabled failed"),
            errors.sdkCodeErrors.map { it.message },
        )
    }

    private class RecordingDoubleGauge(
        override val name: String,
    ) : DoubleGauge {
        val records = mutableListOf<Pair<Double, Map<String, Any>>>()

        override val unit: String? = null
        override val description: String? = null

        override fun enabled(): Boolean = true

        override fun record(value: Double, attributes: (AttributesMutator.() -> Unit)?) {
            val container = CompatAttributesModel().apply { attributes?.invoke(this) }
            records += value to container.attributes
        }
    }

    private class ThrowingJavaDoubleGauge : io.opentelemetry.kotlin.aliases.OtelJavaDoubleGauge {
        override fun isEnabled(): Boolean = error("sdk")
        override fun set(value: Double): Unit = error("sdk")
        override fun set(value: Double, attributes: OtelJavaAttributes): Unit = error("sdk")
        override fun set(
            value: Double,
            attributes: OtelJavaAttributes,
            context: io.opentelemetry.kotlin.aliases.OtelJavaContext,
        ): Unit = error("sdk")
    }
}
