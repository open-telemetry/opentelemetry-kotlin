package io.opentelemetry.kotlin.encode

import io.opentelemetry.kotlin.framework.serialization.SerializableSpanData
import io.opentelemetry.kotlin.framework.serialization.conversion.toSerializable
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlinx.serialization.json.Json
import okio.blackholeSink
import okio.buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class JsonSpanEncoderTest {

    @Test
    fun `should successfully encode span data in JSON format`() {
        // given
        val encoder = JsonSpanEncoder()
        val sink = blackholeSink().buffer()

        // when
        val result = encoder.encode(FakeSpanData(), sink)

        // then
        assertTrue {
            result.any {
                it == Json.encodeToString(
                    Json.parseToJsonElement(spanDataJson)
                )
            }
        }
    }

    @Test
    fun `should successfully decode a log record data in JSON format`() {
        // given
        val encoder = JsonSpanEncoder()
        val value = FakeSpanData()
        val sink = blackholeSink().buffer()

        // when
        val result = encoder.encode(value, sink)

        // then
        assertEquals(
            expected = value.toSerializable(),
            actual = Json.decodeFromString<SerializableSpanData>(result.toList().first())
        )
    }

    private val spanDataJson = """
    {
      "name": "span",
      "kind": "INTERNAL",
      "statusData": {
        "name": "OK",
        "description": ""
      },
      "spanContext": {
        "traceId": "00000000000000000000000000000000",
        "spanId": "0000000000000000",
        "traceFlags": "01",
        "traceState": {
          "foo": "bar"
        }
      },
      "parentSpanContext": {
        "traceId": "00000000000000000000000000000000",
        "spanId": "0000000000000000",
        "traceFlags": "01",
        "traceState": {
          "foo": "bar"
        }
      },
      "startTimestamp": 1000,
      "attributes": {
        "key": "value"
      },
      "events": [
        {
          "name": "event",
          "attributes": {
            "key": "value"
          },
          "timestamp": 1000,
          "totalAttributesCount": 1
        }
      ],
      "links": [
        {
          "spanContext": {
            "traceId": "00000000000000000000000000000000",
            "spanId": "0000000000000000",
            "traceFlags": "01",
            "traceState": {
              "foo": "bar"
            }
          },
          "attributes": {
            "key": "value"
          },
          "totalAttributeCount": 1
        }
      ],
      "endTimestamp": 2000,
      "ended": true,
      "totalRecordedEvents": 1,
      "totalRecordedLinks": 1,
      "totalAttributeCount": 1,
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
      }
    }
""".trimIndent()
}
