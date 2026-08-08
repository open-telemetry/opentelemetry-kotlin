package io.opentelemetry.kotlin.encode

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

sealed interface OtlpJsonEncoder<in T, S> {
    fun getSerializable(value: T): S
    fun getSerializer(): KSerializer<S>
    fun encode(value: T): Sequence<String> =
        sequence {
            yield(
                value = Json.encodeToString(
                    serializer = getSerializer(),
                    value = getSerializable(value)
                )
            )
        }
}
