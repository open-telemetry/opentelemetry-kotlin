package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * A log record exporter that returns telemetry in JSON Format.
 */
@ExperimentalApi
interface JsonLogRecordExporter : LogRecordExporter