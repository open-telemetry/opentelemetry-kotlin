package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.TraceStateCreationAction
import io.opentelemetry.kotlin.tracing.createInvalidSpanContext
import io.opentelemetry.kotlin.tracing.createSpanContext

@ExperimentalApi
public class TraceStateFactoryImpl : TraceStateFactory {
    override val default: TraceState = createInvalidSpanContext().traceState
}

/**
 * Assembles a [TraceState] in insertion order. If a key is added twice, its value is replaced and
 * its position is kept. Entries that fail W3C validation, or that would exceed the maximum number
 * of entries, are ignored.
 */
@OptIn(ExperimentalApi::class)
internal fun buildTraceState(action: TraceStateCreationAction.() -> Unit): TraceState =
    createSpanContext(ByteArray(0), ByteArray(0)) {
        traceState(action)
    }.traceState
