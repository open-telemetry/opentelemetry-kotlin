package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLogger

@OptIn(ExperimentalApi::class)
internal class OtelJavaLoggerAdapter(private val impl: Logger) : OtelJavaLogger {

    override fun logRecordBuilder(): OtelJavaLogRecordBuilder =
        OtelJavaLogRecordBuilderAdapter(impl)
}
