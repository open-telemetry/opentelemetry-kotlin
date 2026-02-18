package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.logging.model.SeverityNumber

@ExperimentalApi
internal object NoopLogger : Logger {
    override fun enabled(
        context: Context?,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean = false

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
    }
}
