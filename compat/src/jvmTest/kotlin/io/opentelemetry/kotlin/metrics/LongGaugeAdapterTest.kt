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
internal class LongGaugeAdapterTest {

    @Test
    fun javaGaugeAdapterRecordsAttributes() {
        val fake = RecordingLongGauge("queue.depth")
        val adapter = OtelJavaLongGaugeAdapter(fake)

        adapter.set(9, OtelJavaAttributes.builder().put("queue", "secondary").build())

        assertEquals(
            listOf(9L to mapOf<String, Any>("queue" to "secondary")),
            fake.records,
        )
    }

    @Test
    fun javaGaugeAdapterDelegatesValueAndContext() {
        val fake = RecordingLongGauge("queue.depth")
        val adapter = OtelJavaLongGaugeAdapter(fake)

        adapter.set(3)
        adapter.set(4, OtelJavaAttributes.empty())
        adapter.set(5, OtelJavaAttributes.empty(), io.opentelemetry.kotlin.aliases.OtelJavaContext.root())

        assertTrue(adapter.isEnabled)
        assertEquals(listOf(3L to emptyMap(), 4L to emptyMap(), 5L to emptyMap()), fake.records)
    }

    @Test
    fun kotlinGaugeAdapterBuildsAndForwardsMetadata() {
        val meter = FakeMeter("test")
        val builder = OtelJavaLongGaugeBuilderAdapter(meter, "queue.depth")

        assertSame(builder, builder.setUnit("{item}"))
        assertSame(builder, builder.setDescription("Current queue depth"))
        val gauge = builder.build()

        assertTrue(gauge.isEnabled)
        assertEquals("{item}", meter.longGauges.single().unit)
        assertEquals("Current queue depth", meter.longGauges.single().description)
    }

    @Test
    fun kotlinGaugeAdapterReportsUserAndSdkErrors() {
        val errors = FakeSdkErrorHandler()
        val throwing = ThrowingJavaLongGauge()
        val adapter = LongGaugeAdapter(throwing, "queue.depth", null, null, errors)

        adapter.record(1) { error("user") }
        adapter.record(1)
        assertFalse(adapter.enabled())

        assertEquals(
            listOf("LongGauge.record failed"),
            errors.userCodeErrors.map { it.message },
        )
        assertEquals(
            listOf("LongGauge.record failed", "LongGauge.enabled failed"),
            errors.sdkCodeErrors.map { it.message },
        )
    }

    private class RecordingLongGauge(
        override val name: String,
    ) : LongGauge {
        val records = mutableListOf<Pair<Long, Map<String, Any>>>()

        override val unit: String? = null
        override val description: String? = null

        override fun enabled(): Boolean = true

        override fun record(value: Long, attributes: AttributesMutator.() -> Unit) {
            val container = CompatAttributesModel().apply(attributes)
            records += value to container.attributes
        }
    }

    private class ThrowingJavaLongGauge : io.opentelemetry.kotlin.aliases.OtelJavaLongGauge {
        override fun isEnabled(): Boolean = error("sdk")
        override fun set(value: Long): Unit = error("sdk")
        override fun set(value: Long, attributes: OtelJavaAttributes): Unit = error("sdk")
        override fun set(
            value: Long,
            attributes: OtelJavaAttributes,
            context: io.opentelemetry.kotlin.aliases.OtelJavaContext,
        ): Unit = error("sdk")
    }
}
