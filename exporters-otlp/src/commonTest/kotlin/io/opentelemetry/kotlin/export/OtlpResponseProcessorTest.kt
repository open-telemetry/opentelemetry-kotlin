package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Verifies the transport-independent classification of OTLP/HTTP responses:
 * https://opentelemetry.io/docs/specs/otlp/#otlphttp-response
 */
internal class OtlpResponseProcessorTest {

    private val errorHandler = FakeSdkErrorHandler()
    private val processor = OtlpResponseProcessor(errorHandler)

    @Test
    fun testSuccessStatusCodes() {
        listOf(200, 204, 299).forEach { code ->
            assertEquals(OtlpResponse.Success, processor.classify(response(code), { null }), "status $code")
        }
        assertTrue(errorHandler.errors.isEmpty())
    }

    @Test
    fun testPartialSuccess() {
        val result = processor.classify(
            response(200),
            { OtlpPartialSuccess(rejectedCount = 2, errorMessage = "2 spans rejected") },
        )
        val partialSuccess = assertIs<OtlpResponse.PartialSuccess>(result)
        assertEquals(2, partialSuccess.rejectedCount)
        assertEquals("2 spans rejected", partialSuccess.errorMessage)
    }

    @Test
    fun testPartialSuccessWithNon200SuccessStatus() {
        val result = processor.classify(
            response(202),
            { OtlpPartialSuccess(rejectedCount = 1, errorMessage = "span rejected") },
        )
        assertEquals(1, assertIs<OtlpResponse.PartialSuccess>(result).rejectedCount)
    }

    @Test
    fun testRetryableStatusCodes() {
        listOf(429, 502, 503, 504).forEach { code ->
            val result = processor.classify(
                response(code, statusBody("retry later"), retryAfterMs = 5_000),
                { error("partial success must not be parsed for $code") },
            )
            val retryable = assertIs<OtlpResponse.RetryableError>(result, "status $code")
            assertEquals(code, retryable.statusCode)
            assertEquals(5_000, retryable.retryAfterMs)
            assertEquals("retry later", retryable.errorMessage)
        }
    }

    @Test
    fun testRetryableStatusWithoutRetryAfter() {
        val result = processor.classify(response(503), { null })
        assertNull(assertIs<OtlpResponse.RetryableError>(result).retryAfterMs)
    }

    @Test
    fun testClientErrorBody() {
        val result = processor.classify(
            response(400, statusBody("invalid trace_id")),
            { error("partial success must not be parsed for 400") },
        )
        val clientError = assertIs<OtlpResponse.ClientError>(result)
        assertEquals(400, clientError.statusCode)
        assertEquals("invalid trace_id", clientError.errorMessage)
    }

    @Test
    fun testServerErrorBody() {
        val result = processor.classify(response(500, statusBody("internal error")), { null })
        val serverError = assertIs<OtlpResponse.ServerError>(result)
        assertEquals(500, serverError.statusCode)
        assertEquals("internal error", serverError.errorMessage)
    }

    @Test
    fun testEmptyAndMalformedErrorBodiesHaveNoMessage() {
        listOf(ByteArray(0), byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x42)).forEach { body ->
            assertNull(assertIs<OtlpResponse.ClientError>(processor.classify(response(400, body), { null })).errorMessage)
            assertNull(assertIs<OtlpResponse.ServerError>(processor.classify(response(500, body), { null })).errorMessage)
        }
        assertTrue(errorHandler.errors.isEmpty())
    }

    @Test
    fun testUnexpectedStatus() {
        val result = processor.classify(response(301), { error("partial success must not be parsed for 301") })
        assertEquals(301, assertIs<OtlpResponse.UnexpectedStatus>(result).statusCode)
    }

    @Test
    fun testBodyAtSizeLimitIsClassified() {
        val body = ByteArray(MAX_RESPONSE_BODY_BYTES.toInt())
        assertEquals(OtlpResponse.Success, processor.classify(response(200, body), { null }))
        assertTrue(errorHandler.errors.isEmpty())
    }

    @Test
    fun testOversizedBodyIsRejectedAndReported() {
        val body = ByteArray(MAX_RESPONSE_BODY_BYTES.toInt() + 1)
        val result = processor.classify(response(503, body), { error("oversized body must not be parsed") })
        assertEquals(503, assertIs<OtlpResponse.ResponseTooLarge>(result).statusCode)
        val error = errorHandler.userCodeErrors.single()
        assertEquals("OTLP response discarded (status=503)", error.message)
        assertEquals(
            "OTLP response body exceeded $MAX_RESPONSE_BODY_BYTES bytes",
            error.cause.message,
        )
    }

    private fun response(
        statusCode: Int,
        body: ByteArray = ByteArray(0),
        retryAfterMs: Long? = null,
    ): OtlpHttpResponse = OtlpHttpResponse(statusCode, retryAfterMs, body)

    /**
     * Encodes a google.rpc.Status with `code = 3` and the given message (< 128 bytes).
     */
    private fun statusBody(message: String): ByteArray {
        val encoded = message.encodeToByteArray()
        return byteArrayOf(0x08, 0x03, 0x12, encoded.size.toByte()) + encoded
    }
}
