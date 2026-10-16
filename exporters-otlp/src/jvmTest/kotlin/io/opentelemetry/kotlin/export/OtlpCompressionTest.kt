package io.opentelemetry.kotlin.export

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.export.toProtobufByteArray
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class OtlpCompressionTest {
    @Test
    fun testRequestContainsRealGzipData() = runBlocking {
        val spans = listOf(FakeSpanData())
        val captured = CompletableDeferred<Pair<String?, ByteArray>>()
        val engine = MockEngine { request ->
            captured.complete(request.body.headers[HttpHeaders.ContentEncoding] to request.body.toByteArray())
            respond(content = "", status = HttpStatusCode.OK)
        }
        val httpClient = createDefaultHttpClient(requestTimeoutMs = 5_000, engine = engine)
        try {
            val client = OtlpClient("http://localhost", httpClient, NoopSdkErrorHandler)
            assertIs<OtlpResponse.Success>(client.exportTraces(spans))
            val (encoding, body) = captured.await()
            assertEquals("gzip", encoding)
            val decoded = GZIPInputStream(ByteArrayInputStream(body)).use { it.readBytes() }
            assertContentEquals(spans.toProtobufByteArray(), decoded)
        } finally {
            httpClient.close()
            engine.close()
        }
    }
}
