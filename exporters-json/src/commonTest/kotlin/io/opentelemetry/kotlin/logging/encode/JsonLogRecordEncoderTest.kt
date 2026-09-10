package io.opentelemetry.kotlin.logging.encode

import io.opentelemetry.kotlin.framework.serialization.SerializableLogRecordData
import io.opentelemetry.kotlin.framework.serialization.conversion.toSerializable
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class JsonLogRecordEncoderTest {

    @Test
    fun `should successfully encode a log record data in JSON format`() {
        // given
        val encoder = JsonLogRecordEncoder()
        val value = FakeLogRecordData()

        // when
        val result = encoder.encode(value)

        // then
        assertTrue {
            result == Json.encodeToString(value.toSerializable())
        }
    }

    @Test
    fun `should successfully decode a log record data in JSON format`() {
        // given
        val encoder = JsonLogRecordEncoder()
        val value = FakeLogRecordData()

        // when
        val result = encoder.encode(value)

        // then
        assertEquals(
            expected = value.toSerializable(),
            actual = Json.decodeFromString<SerializableLogRecordData>(result)
        )
    }
}
