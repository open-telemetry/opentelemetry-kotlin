package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.propagation.utils.W3CTraceStateValidator
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.TraceStateImpl
import io.opentelemetry.kotlin.tracing.TracingDsl

@ExperimentalApi
public class TraceStateFactoryImpl : TraceStateFactory {
    override val default: TraceState = TraceStateImpl.EMPTY
}

/**
 * Assembles a [TraceState] in insertion order. If a key is added twice, its value is replaced and
 * its position is kept. Entries that fail W3C validation, or that would exceed the maximum number
 * of entries, are ignored.
 */
@OptIn(ExperimentalApi::class)
internal fun buildTraceState(action: TraceStateCreationAction.() -> Unit): TraceState =
    TraceStateCreationAction().apply(action).build()

/**
 * DSL receiver for [buildTraceState].
 */
@OptIn(ExperimentalApi::class)
@TracingDsl
public class TraceStateCreationAction internal constructor() {

    private val entries = linkedMapOf<String, String>()

    public fun put(key: String, value: String) {
        if (W3CTraceStateValidator.canPut(entries, key, value)) {
            entries[key] = value
        }
    }

    internal fun build(): TraceState = if (entries.isEmpty()) {
        TraceStateImpl.EMPTY
    } else {
        TraceStateImpl(entries)
    }
}
