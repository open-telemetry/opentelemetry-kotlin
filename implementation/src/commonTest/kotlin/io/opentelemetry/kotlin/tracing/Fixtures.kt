package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.init.config.SpanLimitConfig

internal val fakeSpanLimitsConfig = SpanLimitConfig(
    attributeCountLimit = 100,
    attributeValueLengthLimit = Int.MAX_VALUE,
    linkCountLimit = 100,
    eventCountLimit = 100,
    attributeCountPerEventLimit = 100,
    attributeCountPerLinkLimit = 100
)

internal val fakeLogLimitsConfig = AttributeLimitsBehavior(
    attributeCountLimit = 100,
    attributeValueLengthLimit = 100,
)
