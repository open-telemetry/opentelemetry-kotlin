package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.tracing.SpanLinkCompatImpl
import io.opentelemetry.kotlin.tracing.model.SpanLink

@OptIn(ExperimentalApi::class)
internal fun OtelJavaLinkData.toOtelKotlinSpanLink(): SpanLink = SpanLinkCompatImpl(
    spanContext.toOtelKotlinSpanContext(),
    CompatAttributesModel(attributes.toBuilder()),
)

@OptIn(ExperimentalApi::class)
internal fun SpanLink.toOtelJavaLinkData(): OtelJavaLinkData =
    OtelJavaLinkData.create(spanContext.toOtelJavaSpanContext(), attrsFromMap(attributes))
