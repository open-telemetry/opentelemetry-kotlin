package io.opentelemetry.kotlin.export

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

internal actual fun createHttpEngine(): HttpClientEngine = Js.create()

internal actual val supportsHttpCompression: Boolean = false
