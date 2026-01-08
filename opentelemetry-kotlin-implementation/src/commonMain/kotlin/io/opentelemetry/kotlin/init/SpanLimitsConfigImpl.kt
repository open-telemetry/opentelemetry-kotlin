package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.init.config.DEFAULT_EVENT_LIMIT
import io.opentelemetry.kotlin.init.config.DEFAULT_LINK_LIMIT

@OptIn(ExperimentalApi::class)
internal class SpanLimitsConfigImpl : SpanLimitsConfigDsl {
    override var attributeCountLimit: Int = DEFAULT_ATTRIBUTE_LIMIT
    override var linkCountLimit: Int = DEFAULT_LINK_LIMIT
    override var eventCountLimit: Int = DEFAULT_EVENT_LIMIT
    override var attributeCountPerEventLimit: Int = DEFAULT_ATTRIBUTE_LIMIT
    override var attributeCountPerLinkLimit: Int = DEFAULT_ATTRIBUTE_LIMIT
}
