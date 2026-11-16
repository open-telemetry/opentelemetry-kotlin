package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanExporter
import io.opentelemetry.kotlin.error.GuardedSdkErrorHandler
import io.opentelemetry.kotlin.error.PlatformSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

/**
 * Converts an opentelemetry-java span exporter to an opentelemetry-kotlin span exporter.
 * This is useful if you wish to use an existing Java exporter whilst using opentelemetry-kotlin.
 */
@ExperimentalApi
public fun OtelJavaSpanExporter.toOtelKotlinSpanExporter(): SpanExporter = SpanExporterAdapter(this)

/**
 * Converts an opentelemetry-kotlin span exporter to an opentelemetry-java span exporter.
 * This is useful if you wish to use a Kotlin exporter from an opentelemetry-java SDK.
 *
 * @param sdkErrorHandler receives errors raised while delegating to the Kotlin exporter.
 * Defaults to [PlatformSdkErrorHandler].
 */
@ExperimentalApi
public fun SpanExporter.toOtelJavaSpanExporter(
    sdkErrorHandler: SdkErrorHandler = PlatformSdkErrorHandler,
): OtelJavaSpanExporter = OtelJavaSpanExporterAdapter(this, GuardedSdkErrorHandler(sdkErrorHandler))
