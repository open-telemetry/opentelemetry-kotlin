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

        // when
        encoder.encode(FakeLogRecordData(), buffer)

        // then
        buffer.readUtf8()
        assertTrue {
            buffer.readUtf8() == Json.encodeToString(
                Json.parseToJsonElement(logRecordJson)
            )
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

    private val logRecordJson = """
    {
      "resource": {
        "schemaUrl": "schemaUrl",
        "attributes": {
          "foo": "bar"
        }
      },
      "instrumentationScopeInfo": {
        "name": "name",
        "version": "version",
        "schemaUrl": "schemaUrl",
        "attributes": {
          "key": "value"
        }
      },
      "timestampEpochNanos": 1000,
      "observedTimestampEpochNanos": 2000,
      "spanContext": {
        "traceId": "00000000000000000000000000000000",
        "spanId": "0000000000000000",
        "traceFlags": "01",
        "traceState": {
          "foo": "bar"
        }
      },
      "severity": "WARN",
      "severityText": "warning",
      "body": "Hello, World!",
      "attributes": {
        "key": "value"
      },
      "totalAttributeCount": 1
    }
""".trimIndent()
}
