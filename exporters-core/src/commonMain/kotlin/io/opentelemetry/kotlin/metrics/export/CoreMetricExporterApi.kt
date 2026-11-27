package io.opentelemetry.kotlin.metrics.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.ExportConfigDsl
import io.opentelemetry.kotlin.metrics.data.AggregationTemporality
import io.opentelemetry.kotlin.metrics.instrument.InstrumentKind
import io.opentelemetry.kotlin.platformLog

/**
 * Creates a metric exporter that outputs metric data to stdout. The destination is configurable
 * via a parameter that defaults to [platformLog].
 *
 * This exporter is intended for debugging and learning purposes. It is not recommended for
 * production use. The output format is not standardized and can change at any time.
 */
@ExperimentalApi
public fun ExportConfigDsl.stdoutMetricExporter(
    logger: (String) -> Unit = ::platformLog,
    temporality: (InstrumentKind) -> AggregationTemporality = { AggregationTemporality.CUMULATIVE },
): MetricExporter = StdoutMetricExporter(sdkErrorHandler, logger, temporality)
