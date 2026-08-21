package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.LogExportConfigDsl

/**
 * Creates a log record processor that copies event log records onto the current span as a span
 * event. A log record is bridged only if it has a non-empty event name and its trace ID and span ID
 * match those of a currently recording span. Bridged log records still travel through the rest of
 * the log pipeline.
 *
 * See https://opentelemetry.io/docs/specs/otel/logs/sdk/#event-to-span-event-bridge
 */
@ExperimentalApi
public fun LogExportConfigDsl.spanEventBridgeLogRecordProcessor(): LogRecordProcessor =
    SpanEventBridgeLogRecordProcessor()
