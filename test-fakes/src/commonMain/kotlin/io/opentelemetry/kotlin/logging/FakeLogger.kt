package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber

class FakeLogger(
    val name: String,
    var enabledResult: () -> Boolean = { true },
) : Logger {

    val logs: MutableList<FakeReadableLogRecord> = mutableListOf()

    override fun enabled(
        context: Context?,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean = enabledResult()

    override fun emit(
        body: String?,
        eventName: String?,
        timestamp: Long?,
        observedTimestamp: Long?,
        context: Context?,
        severityNumber: SeverityNumber?,
        severityText: String?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        processTelemetry(eventName, timestamp, observedTimestamp, severityNumber, severityText, body)
    }

    private fun processTelemetry(
        eventName: String?,
        timestamp: Long?,
        observedTimestamp: Long?,
        severityNumber: SeverityNumber?,
        severityText: String?,
        body: String?
    ) {
        eventName.toString()
        logs.add(
            FakeReadableLogRecord(
                timestamp,
                observedTimestamp,
                severityNumber,
                severityText,
                body,
                eventName,
            )
        )
    }
}
