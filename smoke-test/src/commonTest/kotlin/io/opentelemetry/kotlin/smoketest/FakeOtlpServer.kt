package io.opentelemetry.kotlin.smoketest

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.export.toReadableLogRecordList
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.toSpanDataList
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Fake OTLP server that collects exported telemetry with Ktor's MockEngine.
 */
@OptIn(ExperimentalApi::class)
class FakeOtlpServer {

    private val spans = mutableListOf<SpanData>()
    private val logs = mutableListOf<ReadableLogRecord>()
    private val timeSource = TimeSource.Monotonic

    /**
     * Mock HTTP engine that intercepts OTLP requests and collects telemetry data.
     */
    val mockEngine = MockEngine { request ->
        val body = request.body.toByteArray()
        val path = request.url.encodedPath

        when {
            path.contains("/v1/traces") -> spans.addAll(body.toSpanDataList())
            path.contains("/v1/logs") -> logs.addAll(body.toReadableLogRecordList())
            else -> error("Unsupported path: $path")
        }

        respond(
            content = ByteReadChannel(""),
            status = HttpStatusCode.OK
        )
    }

    fun close() {
        mockEngine.close()
    }

    /**
     * Base URL for the fake OTLP server.
     */
    val baseUrl: String = "http://localhost:4318"

    /**
     * Waits for telemetry by polling until the predicate matches or the timeout is met.
     */
    private suspend fun <T> awaitTelemetry(
        collection: List<T>,
        timeout: Duration,
        predicate: (T) -> Boolean,
    ): T {
        val start = timeSource.markNow()
        val interval = 1.milliseconds

        while (start.elapsedNow() < timeout) {
            val element = collection.firstOrNull(predicate)
            if (element != null) {
                return element
            }
            delay(interval)
        }
        error("No telemetry matching predicate received within $timeout. Received ${collection.size}.")
    }

    /**
     * Waits for a span matching the predicate by polling until the given timeout.
     */
    suspend fun awaitSpan(
        timeout: Duration = 5.seconds,
        predicate: (SpanData) -> Boolean
    ): SpanData = awaitTelemetry(
        collection = spans,
        timeout = timeout,
        predicate = predicate,
    )

    /**
     * Waits for a log record matching the predicate by polling until the given timeout.
     */
    suspend fun awaitLog(
        timeout: Duration = 5.seconds,
        predicate: (ReadableLogRecord) -> Boolean
    ): ReadableLogRecord = awaitTelemetry(
        collection = logs,
        timeout = timeout,
        predicate = predicate,
    )

    /**
     * Returns all collected spans.
     */
    fun getSpans(): List<SpanData> = spans.toList()

    /**
     * Returns all collected log records.
     */
    fun getLogs(): List<ReadableLogRecord> = logs.toList()
}
