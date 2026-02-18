package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber

@OptIn(ExperimentalApi::class)
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

    @Deprecated(
        "Deprecated",
        replaceWith = ReplaceWith(
            "emit(body, eventName, timestamp, observedTimestamp, context, severityNumber, severityText, attributes)",
            "io.opentelemetry.kotlin.logging.model.SeverityNumber"
        )
    )
    override fun log(
        body: String?,
        timestamp: Long?,
        observedTimestamp: Long?,
        context: Context?,
        severityNumber: SeverityNumber?,
        severityText: String?,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ) {
        processTelemetry(null, timestamp, observedTimestamp, severityNumber, severityText, body)
    }

    @Deprecated(
        "Deprecated",
        replaceWith = ReplaceWith(
            "emit(body, eventName, timestamp, observedTimestamp, context, severityNumber, severityText, attributes)",
            "io.opentelemetry.kotlin.logging.model.SeverityNumber"
        )
    )
    override fun logEvent(
        eventName: String,
        body: String?,
        timestamp: Long?,
        observedTimestamp: Long?,
        context: Context?,
        severityNumber: SeverityNumber?,
        severityText: String?,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ) {
        processTelemetry(eventName, timestamp, observedTimestamp, severityNumber, severityText, body)
    }

    override fun emit(
        body: String?,
        eventName: String?,
        timestamp: Long?,
        observedTimestamp: Long?,
        context: Context?,
        severityNumber: SeverityNumber?,
        severityText: String?,
        attributes: (MutableAttributeContainer.() -> Unit)?
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
