package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.metrics.instrument.InstrumentDescriptor
import io.opentelemetry.kotlin.metrics.instrument.InstrumentKind
import io.opentelemetry.kotlin.metrics.instrument.InstrumentValueType
import io.opentelemetry.kotlin.metrics.view.InstrumentSelector
import io.opentelemetry.kotlin.metrics.view.View
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MetricDescriptorTest {

    @Test
    fun `uses instrument name and description when the view omits them`() {
        val descriptor = MetricDescriptor.create(
            sourceInstrument = instrument,
            view = view(),
        )

        assertEquals(instrument.name, descriptor.name)
        assertEquals(instrument.description, descriptor.description)
    }

    @Test
    fun `uses name and description configured by the view`() {
        val descriptor = MetricDescriptor.create(
            sourceInstrument = instrument,
            view = view(name = "view-name", description = "view-description"),
        )

        assertEquals("view-name", descriptor.name)
        assertEquals("view-description", descriptor.description)
    }

    @Test
    fun `selector does not participate in resolved descriptor equality`() {
        val first = MetricDescriptor.create(
            sourceInstrument = instrument,
            view = view(selector = { _, _ -> true }),
        )
        val second = MetricDescriptor.create(
            sourceInstrument = instrument,
            view = view(selector = { _, _ -> true }),
        )

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    private fun view(
        selector: InstrumentSelector = InstrumentSelector { _, _ -> true },
        name: String? = null,
        description: String? = null,
    ): View = View(
        selector = selector,
        name = name,
        description = description,
    )

    private companion object {
        val instrument = InstrumentDescriptor(
            name = "instrument-name",
            unit = "ms",
            description = "instrument-description",
            kind = InstrumentKind.HISTOGRAM,
            valueType = InstrumentValueType.DOUBLE,
        )
    }
}
