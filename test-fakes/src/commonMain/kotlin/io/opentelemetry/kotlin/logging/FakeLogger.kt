package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData

class FakeLogger(
    val name: String,
    var enabledResult: () -> Boolean = { true },
) : Logger {

    val logs: MutableList<FakeLogRecordData> = mutableListOf()

    override fun enabled(
        context: Context?,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean = enabledResult()

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
        processTelemetry(
            eventName,
            timestamp,
            observedTimestamp,
            severityNumber,
            severityText,
            body,
            attributes
        )
    }

    private fun processTelemetry(
        eventName: String?,
        timestamp: Long?,
        observedTimestamp: Long?,
        severityNumber: SeverityNumber?,
        severityText: String?,
        body: Any?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        eventName.toString()
        logs.add(
            FakeLogRecordData(
                timestamp,
                observedTimestamp,
                severityNumber,
                severityText,
                body,
                eventName,
                attributes?.let { FakeAttributesMutator().apply(it).attributes }
                    ?: FakeLogRecordData().attributes,
            )
        )
    }
}
