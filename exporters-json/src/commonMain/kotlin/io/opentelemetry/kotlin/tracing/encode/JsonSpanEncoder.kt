package io.opentelemetry.kotlin.tracing.encode

import io.opentelemetry.kotlin.encode.OtlpJsonEncoder
import io.opentelemetry.kotlin.framework.serialization.SerializableSpanData
import io.opentelemetry.kotlin.framework.serialization.conversion.toSerializable
import io.opentelemetry.kotlin.tracing.data.SpanData
import kotlinx.serialization.KSerializer

internal class JsonSpanEncoder : OtlpJsonEncoder<SpanData, SerializableSpanData> {
    override fun getSerializable(value: SpanData): SerializableSpanData =
        value.toSerializable()

    override fun getSerializer(): KSerializer<SerializableSpanData> =
        SerializableSpanData.serializer()
}