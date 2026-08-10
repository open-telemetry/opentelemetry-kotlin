package io.opentelemetry.kotlin.encode

import io.opentelemetry.kotlin.framework.serialization.SerializableLogRecordData
import io.opentelemetry.kotlin.framework.serialization.conversion.toSerializable
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import kotlinx.serialization.KSerializer

internal class JsonLogRecordEncoder : OtlpJsonEncoder<ReadableLogRecord, SerializableLogRecordData> {
    override fun getSerializable(value: ReadableLogRecord): SerializableLogRecordData =
        value.toSerializable()

    override fun getSerializer(): KSerializer<SerializableLogRecordData> =
        SerializableLogRecordData.serializer()
}
