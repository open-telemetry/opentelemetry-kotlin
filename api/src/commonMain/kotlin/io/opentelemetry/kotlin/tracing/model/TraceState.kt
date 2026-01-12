package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Holds key-pairs that describe vendor-specific information about a given trace.
 * TraceState is immutable - all mutating operations return a new TraceState instance.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#tracestate
 */
@ThreadSafe
@ExperimentalApi
public interface TraceState {

    /**
     * Returns the value associated with the given key, or null if the key is not present.
     */
    @ThreadSafe
    public fun get(key: String): String?

    /**
     * Returns the trace state as a map of key-value pairs.
     */
    @ThreadSafe
    public fun asMap(): Map<String, String>

    /**
     * Returns a new TraceState with the given key-value pair added.
     * If the key already exists, its value is updated.
     *
     * If the key or the value are invalid, the same TraceState instance is returned.
     *
     * Key requirements (according to https://www.w3.org/TR/trace-context/#key):
     * - Must not be blank
     * - Must be max 256 characters
     * - Simple keys: Must begin with a lowercase letter or digit, can contain a-z, 0-9, _, -, *, /
     * - Multi-tenant keys: Format "tenant@system" where:
     *   - Tenant: max 241 chars, begins with lowercase letter or digit, can contain a-z, 0-9, _, -, *, /
     *   - System: max 14 chars, begins with lowercase letter, can contain a-z, 0-9, _, -, *, /
     *
     * Value requirements:
     * - Must be max 256 characters
     * - Must contain only printable ASCII characters (0x20-0x7E) except comma and equals
     */
    @ThreadSafe
    public fun put(key: String, value: String): TraceState

    /**
     * Returns a new TraceState with the given key removed.
     * If the key doesn't exist, returns the same TraceState.
     */
    @ThreadSafe
    public fun remove(key: String): TraceState
}
