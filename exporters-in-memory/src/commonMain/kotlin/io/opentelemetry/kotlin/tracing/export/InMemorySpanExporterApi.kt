@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Creates an in-memory span exporter that stores telemetry in memory.
 * This is intended for development/testing rather than production use.
 */
@ExperimentalApi
public fun createInMemorySpanExporter(): InMemorySpanExporter = InMemorySpanExporterImpl()
