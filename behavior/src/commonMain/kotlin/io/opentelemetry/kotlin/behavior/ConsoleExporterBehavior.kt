package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Selecting the console exporter. This type has no fields: choosing it is the whole configuration.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk_exporters/stdout/
 * https://opentelemetry.io/docs/specs/otel/logs/sdk_exporters/stdout/
 */
@ExperimentalApi
class ConsoleExporterBehavior : Behavior<ConsoleExporterBehavior> {

    override fun mergeWith(higher: ConsoleExporterBehavior): ConsoleExporterBehavior = higher

    override fun equals(other: Any?): Boolean = other is ConsoleExporterBehavior

    override fun hashCode(): Int = 0
}
