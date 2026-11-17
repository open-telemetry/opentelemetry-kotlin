package io.opentelemetry.kotlin.export

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

internal actual val httpEngineFactory: HttpClientEngineFactory<*> = Darwin

internal actual val supportsHttpCompression: Boolean = false
