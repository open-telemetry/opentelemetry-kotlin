package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context

internal class RecordingLogger(private val enabledResult: Boolean = true) : Logger {

    class EnabledCall(val context: Context?, val severityNumber: SeverityNumber?, val eventName: String?)

    val enabledCalls = mutableListOf<EnabledCall>()
    val emittedExceptions = mutableListOf<Throwable?>()

    override fun enabled(
        context: Context?,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean {
        enabledCalls += EnabledCall(context, severityNumber, eventName)
        return enabledResult
    }

    override fun emit(
        body: Any?,
        eventName: String?,
        timestamp: Long?,
        observedTimestamp: Long?,
        context: Context?,
        severityNumber: SeverityNumber?,
        severityText: String?,
        exception: Throwable?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        emittedExceptions += exception
    }
}
