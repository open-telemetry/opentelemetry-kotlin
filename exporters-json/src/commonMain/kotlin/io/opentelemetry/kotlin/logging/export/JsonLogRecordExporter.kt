package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.JsonExporter

/*
* A log record exporter that returns telemetry in JSON Format.
*/
@ExperimentalApi
abstract class JsonLogRecordExporter : JsonExporter(), LogRecordExporter