package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * The default implementation of [SpanContextFactory].
 */
@OptIn(ExperimentalApi::class)
public object DefaultSpanContextFactory :
    SpanContextFactory by SpanContextFactoryImpl(DefaultTraceFlagsFactory, DefaultTraceStateFactory)
