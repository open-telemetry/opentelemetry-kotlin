package io.opentelemetry.kotlin.encode

import io.opentelemetry.kotlin.framework.serialization.SerializableLogRecordData
import io.opentelemetry.kotlin.framework.serialization.conversion.toSerializable
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import kotlinx.serialization.json.Json
import okio.blackholeSink
import okio.buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class JsonLogRecordEncoderTest {

    @Test
    fun `should successfully encode a log record data in JSON format`() {
        // given
        val encoder = JsonLogRecordEncoder()
        val sink = blackholeSink().buffer()

        // when
        val result = encoder.encode(FakeReadableLogRecord(), sink)

        // then
        assertTrue {
            result.any {
                it == Json.encodeToString(
                    Json.parseToJsonElement(logRecordJson)
                )
            }
        }
    }

    @Test
    fun `should successfully decode a log record data in JSON format`() {
        // given
        val encoder = JsonLogRecordEncoder()
        val value = FakeReadableLogRecord()
        val sink = blackholeSink().buffer()

        // when
        val result = encoder.encode(value, sink)

        // then
        assertEquals(
            expected = value.toSerializable(),
            actual = Json.decodeFromString<SerializableLogRecordData>(result.toList().first())
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
