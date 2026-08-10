package io.opentelemetry.kotlin.encode

import io.opentelemetry.kotlin.framework.serialization.conversion.toSerializable
import io.opentelemetry.kotlin.tracing.data.SpanData
import kotlinx.serialization.json.Json

class JsonSpanEncoder : OtlpJsonEncoder<SpanData> {
    override fun encode(value: SpanData): Sequence<String> =
        sequence {
            yield(Json.encodeToString(value.toSerializable()))
        }
}
