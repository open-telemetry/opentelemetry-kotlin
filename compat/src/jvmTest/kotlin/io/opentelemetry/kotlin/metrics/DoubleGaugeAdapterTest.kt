package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import kotlin.test.Test
import kotlin.test.assertEquals

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

    private class RecordingDoubleGauge(
        override val name: String,
    ) : DoubleGauge {
        val records = mutableListOf<Pair<Double, Map<String, Any>>>()

        override val unit: String? = null
        override val description: String? = null

        override fun enabled(): Boolean = true

        override fun record(value: Double, attributes: AttributesMutator.() -> Unit) {
            val container = CompatAttributesModel().apply(attributes)
            records += value to container.attributes
        }
    }
}
