package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.OtlpExporter
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.OtlpHttpExporter
import kotlin.collections.ifEmpty
import kotlin.collections.orEmpty
import kotlin.collections.plus

@ExperimentalApi
fun OtlpHttpExporter.toBehavior(): OtlpHttpLogsExporterBehavior {
    // if there are duplicate keys, the last one wins.
    // The spec says that in the case of duplicate keys, [headers] have a higher precedence.
    val headers = OtlpExporter.buildHeaderMap(headersList).orEmpty() +
        OtlpExporter.buildHeaderMap(
            headers?.joinToString(separator = ",") { pair -> "${pair.name}=${pair.value}" }
        ).orEmpty()
    return OtlpHttpLogsExporterBehavior(
        endpoint = endpoint,
        timeout = timeout,
        headers = headers.ifEmpty { null },
    )
}
