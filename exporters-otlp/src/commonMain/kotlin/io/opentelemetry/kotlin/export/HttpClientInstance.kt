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

/** A reference-counted lease for a client owned by [HttpClientRegistry]. */
internal class HttpClientLease internal constructor(
    val client: HttpClient,
    private val releaseAction: () -> Unit,
) {
    private val released = AtomicBoolean(false)

    fun release() {
        if (released.compareAndSet(expect = false, update = true)) {
            releaseAction()
        }
    }
}

internal object HttpClientRegistry {
    private class SharedClient(
        val key: HttpClientKey,
        val client: HttpClient,
        var users: Int,
    )

    private val clients = ConcurrentMap<HttpClientKey, SharedClient>()
    private val defaultEngine: HttpClientEngine by lazy { createHttpEngine() }
    private val gate = AtomicBoolean(false)

    /**
     * Acquires a reference to an SDK-owned client. The client is closed when the last lease is
     * released.
     */
    fun lease(engine: HttpClientEngine? = null, requestTimeoutMs: Long): HttpClientLease = withGate {
        val key = HttpClientKey(engine ?: defaultEngine, requestTimeoutMs)
        val shared = clients[key]
        if (shared != null) {
            shared.users++
            return@withGate HttpClientLease(shared.client) { release(shared) }
        }

        val created = SharedClient(
            key = key,
            client = createDefaultHttpClient(requestTimeoutMs, key.engine),
            users = 1,
        )
        clients[key] = created
        HttpClientLease(created.client) { release(created) }
    }

    /**
     * Returns a registry client without acquiring an exporter lease. This is retained for tests
     * that inspect the shared-client cache; exporters must use [lease].
     */
    internal fun getOrCreate(engine: HttpClientEngine? = null, requestTimeoutMs: Long): HttpClient = withGate {
        val key = HttpClientKey(engine ?: defaultEngine, requestTimeoutMs)
        clients[key]?.client ?: SharedClient(
            key = key,
            client = createDefaultHttpClient(requestTimeoutMs, key.engine),
            users = 0,
        ).also { clients[key] = it }.client
    }

    /** Returns a client for test inspection without acquiring an exporter lease. */
    internal fun peek(engine: HttpClientEngine? = null, requestTimeoutMs: Long): HttpClient? = withGate {
        clients[HttpClientKey(engine ?: defaultEngine, requestTimeoutMs)]?.client
    }

    /** Closes and removes all SDK-owned clients. Intended for test cleanup. */
    internal fun clear() {
        val toClose = withGate {
            val clientsToClose = clients.values.map { it.client }
            clients.clear()
            clientsToClose
        }
        toClose.forEach { it.close() }
    }

    private fun release(shared: SharedClient) {
        val clientToClose = withGate {
            shared.users--
            if (shared.users > 0 || clients[shared.key] !== shared) {
                return@withGate null
            }
            clients.remove(shared.key)
            shared.client
        }
        clientToClose?.close()
    }

    private inline fun <T> withGate(block: () -> T): T {
        while (!gate.compareAndSet(expect = false, update = true)) {
            // Registry operations are short and only run during exporter creation and shutdown.
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
        if (supportsHttpCompression) {
            gzip()
            deflate()
        } else {
            identity()
        }
    }
}

internal expect fun createHttpEngine(): HttpClientEngine

// Ktor's gzip/deflate encoders on native targets are a no-op
internal expect val supportsHttpCompression: Boolean
