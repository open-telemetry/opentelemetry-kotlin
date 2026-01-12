package io.opentelemetry.kotlin.export

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation

internal val defaultHttpClient by lazy {
    createDefaultHttpClient()
}

internal fun createDefaultHttpClient(
    requestTimeoutMs: Long = 30_000,
    engine: HttpClientEngine = createHttpEngine()
): HttpClient =
    HttpClient(engine) {
        install(HttpTimeout) {
            requestTimeoutMillis = requestTimeoutMs
        }
        install(ContentNegotiation)
        install(ContentEncoding) {
            gzip()
            deflate()
        }
    }

internal expect fun createHttpEngine(): HttpClientEngine
