package io.opentelemetry.kotlin.export

import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpResponseTest {

    @Test
    fun testSuccess() {
        assertEquals(200, OtlpResponse.Success.statusCode)
        assertEquals("Success(statusCode=200)", OtlpResponse.Success.toString())
    }

    @Test
    fun testClientError() {
        val clientError = OtlpResponse.ClientError(400, "bad request")
        assertEquals(400, clientError.statusCode)
        assertEquals(
            "ClientError(errorMessage=bad request, statusCode=400)",
            clientError.toString()
        )
        assertEquals("bad request", clientError.errorMessage)
    }

    @Test
    fun testServerErrorCode() {
        assertEquals(503, OtlpResponse.ServerError(503, null).statusCode)
    }

    @Test
    fun testRetryableError() {
        val retryable = OtlpResponse.RetryableError(429, 1500L, "slow down")
        assertEquals(429, retryable.statusCode)
        assertEquals(1500L, retryable.retryAfterMs)
        assertEquals("slow down", retryable.errorMessage)
        assertEquals(
            "RetryableError(errorMessage=slow down, retryAfterMs=1500, statusCode=429)",
            retryable.toString()
        )
    }

    @Test
    fun testUnknownCode() {
        assertEquals(-1, OtlpResponse.Unknown.statusCode)
    }
}
