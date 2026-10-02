package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLogger
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.context.toOtelKotlinContext

internal class OtelJavaLoggerAdapter(private val impl: Logger) : OtelJavaLogger {

    override fun logRecordBuilder(): OtelJavaLogRecordBuilder =
        OtelJavaLogRecordBuilderAdapter(impl)

    override fun isEnabled(severity: OtelJavaSeverity, context: OtelJavaContext): Boolean =
        impl.enabled(
            context = context.toOtelKotlinContext(),
            severityNumber = severity.toOtelKotlinSeverityNumber(),
        )

    override fun isEnabled(severity: OtelJavaSeverity): Boolean =
        impl.enabled(severityNumber = severity.toOtelKotlinSeverityNumber())
}
