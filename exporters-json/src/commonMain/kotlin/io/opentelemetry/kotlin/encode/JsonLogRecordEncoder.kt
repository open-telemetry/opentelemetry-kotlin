package io.opentelemetry.kotlin.encode

import io.opentelemetry.kotlin.framework.serialization.conversion.toSerializable
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import kotlinx.serialization.json.Json

internal class JsonLogRecordEncoder : OtlpJsonEncoder<ReadableLogRecord> {
    override fun encode(value: ReadableLogRecord): Sequence<String> =
        sequence {
            yield(Json.encodeToString(value.toSerializable()))
        }
}
