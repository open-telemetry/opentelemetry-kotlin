package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.logging.model.SeverityNumber

@OptIn(ExperimentalApi::class)
internal fun SeverityNumber.toOtelJavaSeverityNumber(): OtelJavaSeverity = when (this) {
    SeverityNumber.UNKNOWN -> OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER
    SeverityNumber.TRACE -> OtelJavaSeverity.TRACE
    SeverityNumber.TRACE2 -> OtelJavaSeverity.TRACE2
    SeverityNumber.TRACE3 -> OtelJavaSeverity.TRACE3
    SeverityNumber.TRACE4 -> OtelJavaSeverity.TRACE4
    SeverityNumber.DEBUG -> OtelJavaSeverity.DEBUG
    SeverityNumber.DEBUG2 -> OtelJavaSeverity.DEBUG2
    SeverityNumber.DEBUG3 -> OtelJavaSeverity.DEBUG3
    SeverityNumber.DEBUG4 -> OtelJavaSeverity.DEBUG4
    SeverityNumber.INFO -> OtelJavaSeverity.INFO
    SeverityNumber.INFO2 -> OtelJavaSeverity.INFO2
    SeverityNumber.INFO3 -> OtelJavaSeverity.INFO3
    SeverityNumber.INFO4 -> OtelJavaSeverity.INFO4
    SeverityNumber.WARN -> OtelJavaSeverity.WARN
    SeverityNumber.WARN2 -> OtelJavaSeverity.WARN2
    SeverityNumber.WARN3 -> OtelJavaSeverity.WARN3
    SeverityNumber.WARN4 -> OtelJavaSeverity.WARN4
    SeverityNumber.ERROR -> OtelJavaSeverity.ERROR
    SeverityNumber.ERROR2 -> OtelJavaSeverity.ERROR2
    SeverityNumber.ERROR3 -> OtelJavaSeverity.ERROR3
    SeverityNumber.ERROR4 -> OtelJavaSeverity.ERROR4
    SeverityNumber.FATAL -> OtelJavaSeverity.FATAL
    SeverityNumber.FATAL2 -> OtelJavaSeverity.FATAL2
    SeverityNumber.FATAL3 -> OtelJavaSeverity.FATAL3
    SeverityNumber.FATAL4 -> OtelJavaSeverity.FATAL4
}

@OptIn(ExperimentalApi::class)
internal fun OtelJavaSeverity.toOtelKotlinSeverityNumber(): SeverityNumber = when (this) {
    OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER -> SeverityNumber.UNKNOWN
    OtelJavaSeverity.TRACE -> SeverityNumber.TRACE
    OtelJavaSeverity.TRACE2 -> SeverityNumber.TRACE2
    OtelJavaSeverity.TRACE3 -> SeverityNumber.TRACE3
    OtelJavaSeverity.TRACE4 -> SeverityNumber.TRACE4
    OtelJavaSeverity.DEBUG -> SeverityNumber.DEBUG
    OtelJavaSeverity.DEBUG2 -> SeverityNumber.DEBUG2
    OtelJavaSeverity.DEBUG3 -> SeverityNumber.DEBUG3
    OtelJavaSeverity.DEBUG4 -> SeverityNumber.DEBUG4
    OtelJavaSeverity.INFO -> SeverityNumber.INFO
    OtelJavaSeverity.INFO2 -> SeverityNumber.INFO2
    OtelJavaSeverity.INFO3 -> SeverityNumber.INFO3
    OtelJavaSeverity.INFO4 -> SeverityNumber.INFO4
    OtelJavaSeverity.WARN -> SeverityNumber.WARN
    OtelJavaSeverity.WARN2 -> SeverityNumber.WARN2
    OtelJavaSeverity.WARN3 -> SeverityNumber.WARN3
    OtelJavaSeverity.WARN4 -> SeverityNumber.WARN4
    OtelJavaSeverity.ERROR -> SeverityNumber.ERROR
    OtelJavaSeverity.ERROR2 -> SeverityNumber.ERROR2
    OtelJavaSeverity.ERROR3 -> SeverityNumber.ERROR3
    OtelJavaSeverity.ERROR4 -> SeverityNumber.ERROR4
    OtelJavaSeverity.FATAL -> SeverityNumber.FATAL
    OtelJavaSeverity.FATAL2 -> SeverityNumber.FATAL2
    OtelJavaSeverity.FATAL3 -> SeverityNumber.FATAL3
    OtelJavaSeverity.FATAL4 -> SeverityNumber.FATAL4
}
