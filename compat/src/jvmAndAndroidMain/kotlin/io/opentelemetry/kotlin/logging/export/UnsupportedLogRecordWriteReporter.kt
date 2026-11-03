package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import java.util.Collections

/**
 * Reports writes to [io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord] properties that
 * opentelemetry-java can't apply. Each property is reported at most once per processor.
 */
internal class UnsupportedLogRecordWriteReporter(
    private val sdkErrorHandler: SdkErrorHandler,
) {

    private val reported: MutableSet<String> = Collections.synchronizedSet(mutableSetOf())

    fun report(property: String) {
        if (!reported.add(property)) {
            return
        }
        sdkErrorHandler.reportError(
            SdkError.ApiMisuse(
                api = "ReadWriteLogRecord.$property",
                message = "opentelemetry-java does not support changing $property, so the new value was ignored. " +
                    "Only attributes can be changed in compat mode.",
                severity = SdkErrorSeverity.WARNING,
            )
        )
    }
}
