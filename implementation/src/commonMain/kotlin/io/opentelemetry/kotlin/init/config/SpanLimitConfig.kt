package io.opentelemetry.kotlin.init.config

import io.opentelemetry.kotlin.ThreadSafe

/**
 * Limits on span data capture.
 */
@ThreadSafe
internal class SpanLimitConfig(

    /**
     * Max attribute count.
     */
    val attributeCountLimit: Int,

    /**
     * Max link count.
     */
    val linkCountLimit: Int,

    /**
     * Max event count.
     */
    val eventCountLimit: Int,

    /**
     * Max attributes per event.
     */
    val attributeCountPerEventLimit: Int,

    /**
     * Max attributes per link.
     */
    val attributeCountPerLinkLimit: Int,
)

internal const val DEFAULT_LINK_LIMIT: Int = 128
internal const val DEFAULT_EVENT_LIMIT: Int = 128
