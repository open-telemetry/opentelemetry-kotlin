package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.metrics.instrument.InstrumentDescriptor
import io.opentelemetry.kotlin.metrics.instrument.SdkInstrument

internal class LongUpDownCounterImpl(
    override val descriptor: InstrumentDescriptor,
) : LongUpDownCounter, SdkInstrument {
    override fun enabled(): Boolean = true
    override fun add(value: Long, attributes: (AttributesMutator.() -> Unit)?) = Unit
}
