package io.opentelemetry.kotlin.export

import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.util.GZipEncoder
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.toByteArray
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal suspend fun decodeOtlpRequestBody(request: HttpRequestData, body: ByteArray): ByteArray {
    if (supportsHttpCompression) {
        assertEquals("gzip,deflate", request.headers[HttpHeaders.AcceptEncoding])
        assertEquals("gzip", request.body.headers[HttpHeaders.ContentEncoding])
        return GZipEncoder.decode(ByteReadChannel(body)).toByteArray()
    }
    assertEquals("identity", request.headers[HttpHeaders.AcceptEncoding])
    assertNull(request.body.headers[HttpHeaders.ContentEncoding])
    return body
}
