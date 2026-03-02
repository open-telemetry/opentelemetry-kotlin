package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLogger

internal class OtelJavaLoggerAdapter(private val impl: Logger) : OtelJavaLogger {

    override fun logRecordBuilder(): OtelJavaLogRecordBuilder =
        OtelJavaLogRecordBuilderAdapter(impl)
}
