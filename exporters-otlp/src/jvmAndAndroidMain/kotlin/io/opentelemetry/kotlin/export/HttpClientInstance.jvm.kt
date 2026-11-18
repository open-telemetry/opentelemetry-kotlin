package io.opentelemetry.kotlin.export

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual val httpEngineFactory: HttpClientEngineFactory<*> = OkHttp

internal actual val supportsHttpCompression: Boolean = true
