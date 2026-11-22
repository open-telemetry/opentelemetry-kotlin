package io.opentelemetry.kotlin.export

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

internal actual val httpEngineFactory: HttpClientEngineFactory<*> = Js

internal actual val supportsHttpCompression: Boolean = false
