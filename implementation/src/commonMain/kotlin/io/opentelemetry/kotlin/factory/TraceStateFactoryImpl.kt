package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.TraceStateImpl
import io.opentelemetry.kotlin.tracing.model.TraceState

@ExperimentalApi
internal class TraceStateFactoryImpl : TraceStateFactory {
    override val default: TraceState by lazy { TraceStateImpl.create() }
}
