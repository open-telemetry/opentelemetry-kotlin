package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordExporter
import io.opentelemetry.kotlin.error.GuardedSdkErrorHandler
import io.opentelemetry.kotlin.error.PlatformSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

/**
 * Converts an opentelemetry-java log record exporter to an opentelemetry-kotlin log record exporter.
 * This is useful if you wish to use an existing Java exporter whilst using opentelemetry-kotlin.
 */
@ExperimentalApi
public fun OtelJavaLogRecordExporter.toOtelKotlinLogRecordExporter(): LogRecordExporter =
    LogRecordExporterAdapter(this)

/**
 * Converts an opentelemetry-kotlin log record exporter to an opentelemetry-java log record exporter.
 * This is useful if you wish to use a Kotlin exporter from an opentelemetry-java SDK.
 *
 * @param sdkErrorHandler receives errors raised while delegating to the Kotlin exporter.
 * Defaults to [PlatformSdkErrorHandler].
 */
@ExperimentalApi
public fun LogRecordExporter.toOtelJavaLogRecordExporter(
    sdkErrorHandler: SdkErrorHandler = PlatformSdkErrorHandler,
): OtelJavaLogRecordExporter =
    OtelJavaLogRecordExporterAdapter(this, GuardedSdkErrorHandler(sdkErrorHandler))
