package io.opentelemetry.kotlin.metrics.export

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesMutator

public interface LongMetricRecord : AttributeContainer, AttributesMutator {
    public val name: String
    public val type: String
    public val value: Long
    public val unit: String?
    public val description: String?
}
