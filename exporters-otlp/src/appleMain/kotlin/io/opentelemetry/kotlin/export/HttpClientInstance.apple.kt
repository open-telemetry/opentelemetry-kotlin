package io.opentelemetry.kotlin.export

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

internal actual fun createHttpEngine(): HttpClientEngine = Darwin.create()

internal actual val supportsHttpCompression: Boolean = false
