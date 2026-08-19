package io.opentelemetry.kotlin.export

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.util.collections.ConcurrentMap

internal data class HttpClientKey(
    val engine: HttpClientEngine,
    val timeoutMs: Long,
)

internal object HttpClientRegistry {
    private val clients = ConcurrentMap<HttpClientKey, HttpClient>()

    internal fun clear() {
        clients.clear()
    }

    fun getOrCreate(engine: HttpClientEngine, requestTimeoutMs: Long): HttpClient {
        val key = HttpClientKey(engine, requestTimeoutMs)
        return clients.computeIfAbsent(key) {
            createDefaultHttpClient(requestTimeoutMs, engine)
        }
    }
}

internal fun createDefaultHttpClient(
    requestTimeoutMs: Long,
    engine: HttpClientEngine = createHttpEngine(),
): HttpClient = HttpClient(engine) {
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
