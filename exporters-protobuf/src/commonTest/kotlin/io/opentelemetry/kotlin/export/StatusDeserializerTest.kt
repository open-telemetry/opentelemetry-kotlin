package io.opentelemetry.kotlin.export

import com.squareup.wire.ProtoAdapter
import com.squareup.wire.ProtoWriter
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StatusDeserializerTest {

    @Test
    fun testMessage() {
        assertEquals("bad data", status(code = 3, message = "bad data").deserializeStatusMessage())
    }

    @Test
    fun testMessageWithDetails() {
        val bytes = status(code = 3, message = "bad data", details = byteArrayOf(0x0A, 0x00))
        assertEquals("bad data", bytes.deserializeStatusMessage())
    }

    @Test
    fun testNoMessage() {
        assertNull(status(code = 3, message = null).deserializeStatusMessage())
    }

    @Test
    fun testEmptyBody() {
        assertNull(ByteArray(0).deserializeStatusMessage())
    }

    @Test
    fun testMalformedBody() {
        assertNull(byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x42).deserializeStatusMessage())
    }

    private fun status(code: Int, message: String?, details: ByteArray? = null): ByteArray {
        val buffer = Buffer()
        val writer = ProtoWriter(buffer)
        ProtoAdapter.INT32.encodeWithTag(writer, 1, code)
        message?.let { ProtoAdapter.STRING.encodeWithTag(writer, 2, it) }
        details?.let { ProtoAdapter.BYTES.encodeWithTag(writer, 3, okio.ByteString.of(*it)) }
        return buffer.readByteArray()
    }
}
