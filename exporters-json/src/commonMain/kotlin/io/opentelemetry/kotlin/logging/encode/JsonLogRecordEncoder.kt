package io.opentelemetry.kotlin.logging.encode

import io.opentelemetry.kotlin.encode.OtlpJsonEncoder
import io.opentelemetry.kotlin.framework.serialization.SerializableLogRecordData
import io.opentelemetry.kotlin.framework.serialization.conversion.toSerializable
import io.opentelemetry.kotlin.logging.data.LogRecordData
import kotlinx.serialization.KSerializer

internal class JsonLogRecordEncoder : OtlpJsonEncoder<LogRecordData, SerializableLogRecordData> {
    override fun getSerializable(value: LogRecordData): SerializableLogRecordData =
        value.toSerializable()

    override fun getSerializer(): KSerializer<SerializableLogRecordData> =
        SerializableLogRecordData.serializer()
}
