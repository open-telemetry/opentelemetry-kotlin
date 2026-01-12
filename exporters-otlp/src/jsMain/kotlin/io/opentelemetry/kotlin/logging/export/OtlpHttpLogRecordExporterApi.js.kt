package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
public actual fun createOtlpHttpLogRecordExporter(baseUrl: String): LogRecordExporter {
    throw UnsupportedOperationException()
}
