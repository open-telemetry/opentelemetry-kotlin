package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Configures the entries of a [TraceState].
 */
@OptIn(ExperimentalApi::class)
@TracingDsl
internal interface TraceStateCreationAction {

    /**
     * Adds an entry. Entries are kept in insertion order. If a key is added twice, its value is
     * replaced and its position is kept. Entries that fail W3C validation, or that would exceed
     * the maximum number of entries, are ignored.
     */
    fun put(key: String, value: String)
}
