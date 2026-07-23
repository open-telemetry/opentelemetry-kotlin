package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.resource.Resource

internal class MeterImpl(
    val instrumentationScopeInfo: InstrumentationScopeInfo,
    val resource: Resource,
    private val contextFactory: ContextFactory,
) : Meter {

    private val shutdownState: MutableShutdownState = MutableShutdownState()


    override fun createLongCounter(
        name: String,
        description: String?,
        unit: String?
    ): LongCounter {
        return LongCounterImpl(name, description, unit, contextFactory, null, shutdownState)
    }

}
