package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordProcessor

/**
 * Converts an opentelemetry-java log record processor to an opentelemetry-kotlin log record processor.
 * This is useful if you wish to use an existing Java processor whilst using opentelemetry-kotlin.
 */
@OptIn(ExperimentalApi::class)
public fun OtelJavaLogRecordProcessor.toOtelKotlinLogRecordProcessor(): LogRecordProcessor =
    LogRecordProcessorAdapter(this)
