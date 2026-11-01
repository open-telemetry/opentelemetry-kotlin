package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
