package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributeContainer

/**
 * A read-only representation of a span event
 */
@ExperimentalApi
public interface EventData : AttributeContainer {

    /**
     * The name of the event
     */
    @ThreadSafe
    public val name: String

    /**
     * The timestamp of the event in nanoseconds
     */
    @ThreadSafe
    public val timestamp: Long
}
