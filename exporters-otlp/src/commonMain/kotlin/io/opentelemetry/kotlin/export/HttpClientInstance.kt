package io.opentelemetry.kotlin.export

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.compression.ContentEncodingConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.util.collections.ConcurrentMap
import io.opentelemetry.kotlin.AtomicBoolean

internal data class HttpClientKey(
    val engine: HttpClientEngine,
    val timeoutMs: Long,
)

/**
 * A shared [HttpClient] plus the number of exporters still using it.
 * The client is closed when the last lease is released.
 */
internal class HttpClientLease internal constructor(
    val client: HttpClient,
    private val releaseOnce: () -> Unit,
) {
    private val released = AtomicBoolean(false)

    fun release() {
        if (released.compareAndSet(false, true)) {
            releaseOnce()
        }
    }
}

internal object HttpClientRegistry {
    private class SharedClient(val client: HttpClient, var users: Int)

    private val clients = ConcurrentMap<HttpClientKey, SharedClient>()
    private val defaultEngine: HttpClientEngine by lazy { createHttpEngine() }
    private val gate = AtomicBoolean(false)

    internal fun clear() = withGate {
        clients.clear()
    }

    /**
     * Returns the shared client, creating one with no owners when it is absent.
     * Does not keep the client open across shutdown. Exporters take a [lease] instead.
     */
    fun getOrCreate(engine: HttpClientEngine? = null, requestTimeoutMs: Long): HttpClient = withGate {
        val key = HttpClientKey(engine ?: defaultEngine, requestTimeoutMs)
        val existing = clients[key]
        if (existing != null) {
            return@withGate existing.client
        }
        val created = SharedClient(createDefaultHttpClient(requestTimeoutMs, key.engine), users = 0)
        clients[key] = created
        created.client
    }

    /**
     * Returns the shared client for [engine] and [requestTimeoutMs] without taking a lease.
     * Tests use this to observe whether shutdown closed the client.
     */
    fun peek(engine: HttpClientEngine? = null, requestTimeoutMs: Long): HttpClient? = withGate {
        clients[HttpClientKey(engine ?: defaultEngine, requestTimeoutMs)]?.client
    }

    fun lease(engine: HttpClientEngine? = null, requestTimeoutMs: Long): HttpClientLease = withGate {
        val key = HttpClientKey(engine ?: defaultEngine, requestTimeoutMs)
        val existing = clients[key]
        if (existing != null) {
            existing.users += 1
            return@withGate HttpClientLease(existing.client) { releaseShared(existing) }
        }
        val created = SharedClient(createDefaultHttpClient(requestTimeoutMs, key.engine), users = 1)
        clients[key] = created
        HttpClientLease(created.client) { releaseShared(created) }
    }

    private fun releaseShared(shared: SharedClient) {
        val closing = withGate {
            shared.users -= 1
            if (shared.users > 0) {
                return@withGate null
            }
            val key = clients.entries.firstOrNull { it.value === shared }?.key
            if (key == null) {
                null
            } else {
                clients.remove(key)
                shared.client
            }
        }
        closing?.close()
    }

    private inline fun <T> withGate(block: () -> T): T {
        while (!gate.compareAndSet(false, true)) {
            // Registry updates are short and run at exporter start and shutdown.
        }
        try {
            return block()
        } finally {
            gate.set(false)
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
        mode = ContentEncodingConfig.Mode.All
        gzip()
        deflate()
    }
}

internal expect fun createHttpEngine(): HttpClientEngine
