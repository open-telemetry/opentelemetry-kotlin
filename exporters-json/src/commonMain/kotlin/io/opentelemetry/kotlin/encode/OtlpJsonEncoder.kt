package io.opentelemetry.kotlin.encode

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okio.BufferedSink

sealed interface OtlpJsonEncoder<in T, S> {
    fun getSerializable(value: T): S
    fun getSerializer(): KSerializer<S>
    fun encode(value: T, sink: BufferedSink) {
        sink.writeUtf8(Json.encodeToString(
            serializer = getSerializer(),
            value = getSerializable(value)
        ))
    }
}
