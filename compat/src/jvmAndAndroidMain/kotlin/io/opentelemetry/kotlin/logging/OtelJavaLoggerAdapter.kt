package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLogger
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.factory.ContextFactory

internal class OtelJavaLoggerAdapter(
    private val impl: Logger,
    private val contextFactory: ContextFactory,
) : OtelJavaLogger {

    override fun logRecordBuilder(): OtelJavaLogRecordBuilder =
        OtelJavaLogRecordBuilderAdapter(impl, contextFactory)

    override fun isEnabled(severity: OtelJavaSeverity, context: OtelJavaContext): Boolean =
        impl.enabled(
            context = context.toOtelKotlinContext(),
            severityNumber = severity.toOtelKotlinSeverityNumber(),
        )

    override fun isEnabled(severity: OtelJavaSeverity): Boolean =
        impl.enabled(
            context = contextFactory.implicit(),
            severityNumber = severity.toOtelKotlinSeverityNumber(),
        )
}
