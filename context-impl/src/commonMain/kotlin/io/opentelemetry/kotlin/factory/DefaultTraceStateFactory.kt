package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * The default implementation of [TraceStateFactory].
 */
@OptIn(ExperimentalApi::class)
public object DefaultTraceStateFactory : TraceStateFactory by TraceStateFactoryImpl()
