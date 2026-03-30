package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines limits on how much data should be captured in spans.
 */
@ExperimentalApi
@ConfigDsl
public interface SpanLimitsConfigDsl : AttributeLimitsConfigDsl {

    /**
     * The maximum number of links
     */
    public var linkCountLimit: Int

    /**
     * The maximum number of events
     */
    public var eventCountLimit: Int

    /**
     * The maximum number of attributes per event
     */
    public var attributeCountPerEventLimit: Int

    /**
     * The maximum number of attributes per link
     */
    public var attributeCountPerLinkLimit: Int
}
