package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.TraceStateImpl

@ExperimentalApi
public class TraceStateFactoryImpl : TraceStateFactory {
    override val default: TraceState by lazy { TraceStateImpl.create() }
}
