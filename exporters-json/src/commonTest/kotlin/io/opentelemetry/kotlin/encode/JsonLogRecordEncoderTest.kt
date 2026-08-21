package io.opentelemetry.kotlin.encode

import io.opentelemetry.kotlin.framework.serialization.SerializableLogRecordData
import io.opentelemetry.kotlin.framework.serialization.conversion.toSerializable
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import kotlinx.serialization.json.Json
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class JsonLogRecordEncoderTest {

    @Test
    fun `should successfully encode a log record data in JSON format`() {
        // given
        val encoder = JsonLogRecordEncoder()
        val buffer = Buffer()
        val value = FakeLogRecordData()

        // when
        encoder.encode(value, buffer)

        // then
        assertTrue {
            buffer.readUtf8() == Json.encodeToString(value.toSerializable())
        }
    }

    @Test
    fun `should successfully decode a log record data in JSON format`() {
        // given
        val encoder = JsonLogRecordEncoder()
        val value = FakeLogRecordData()
        val buffer = Buffer()

        // when
        encoder.encode(value, buffer)

        // then
        assertEquals(
            expected = value.toSerializable(),
            actual = Json.decodeFromString<SerializableLogRecordData>(buffer.readUtf8())
        )
    }
}
