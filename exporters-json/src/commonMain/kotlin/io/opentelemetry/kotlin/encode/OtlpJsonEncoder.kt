package io.opentelemetry.kotlin.encode

internal sealed interface OtlpJsonEncoder<in T> {
    fun encode(value: T): Sequence<String>
}
