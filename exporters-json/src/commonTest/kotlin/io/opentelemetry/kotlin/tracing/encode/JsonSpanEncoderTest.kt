package io.opentelemetry.kotlin.tracing.encode

import io.opentelemetry.kotlin.framework.serialization.SerializableSpanData
import io.opentelemetry.kotlin.framework.serialization.conversion.toSerializable
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class JsonSpanEncoderTest {

    @Test
    fun `should successfully encode span data in JSON format`() {
        // given
        val encoder = JsonSpanEncoder()
        val value = FakeSpanData()

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
        val encoder = JsonSpanEncoder()
        val value = FakeSpanData()

        // when
        val result = encoder.encode(value)

        // then
        assertEquals(
            expected = value.toSerializable(),
            actual = Json.decodeFromString<SerializableSpanData>(result)
        )
    }
}
