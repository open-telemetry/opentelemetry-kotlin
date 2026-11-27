package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.metrics.instrument.InstrumentDescriptor
import io.opentelemetry.kotlin.metrics.instrument.SdkInstrument

internal class DoubleUpDownCounterImpl(
    override val descriptor: InstrumentDescriptor,
) : DoubleUpDownCounter, SdkInstrument {
    override fun enabled(): Boolean = true
    override fun add(value: Double, attributes: (AttributesMutator.() -> Unit)?) = Unit
}
