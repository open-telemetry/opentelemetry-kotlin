package io.opentelemetry.kotlin.export

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

internal actual val httpEngineFactory: HttpClientEngineFactory<*> = CIO

internal actual val supportsHttpCompression: Boolean = false
