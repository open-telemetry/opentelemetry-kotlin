package io.opentelemetry.kotlin.tracing.contextimpl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.DefaultSpanContextFactory
import io.opentelemetry.kotlin.factory.SpanContextCreationAction
import io.opentelemetry.kotlin.factory.create
import io.opentelemetry.kotlin.tracing.SpanContext

// temporary forwarding functions that will be replaced by api module implementations

@OptIn(ExperimentalApi::class)
internal fun createInvalidSpanContext(): SpanContext = DefaultSpanContextFactory.invalid

@OptIn(ExperimentalApi::class)
internal fun createSpanContext(
    traceId: String,
    spanId: String,
    action: SpanContextCreationAction.() -> Unit = {},
): SpanContext = DefaultSpanContextFactory.create(traceId, spanId, action)

@OptIn(ExperimentalApi::class)
internal fun createSpanContext(
    traceIdBytes: ByteArray,
    spanIdBytes: ByteArray,
    action: SpanContextCreationAction.() -> Unit = {},
): SpanContext = DefaultSpanContextFactory.create(traceIdBytes, spanIdBytes, action)
