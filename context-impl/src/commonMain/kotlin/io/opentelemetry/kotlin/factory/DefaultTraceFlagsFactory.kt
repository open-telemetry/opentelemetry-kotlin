package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * The default implementation of [TraceFlagsFactory].
 */
@OptIn(ExperimentalApi::class)
public object DefaultTraceFlagsFactory : TraceFlagsFactory by TraceFlagsFactoryImpl()
