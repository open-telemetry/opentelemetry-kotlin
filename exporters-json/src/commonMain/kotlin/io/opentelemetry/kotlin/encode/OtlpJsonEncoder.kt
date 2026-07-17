package io.opentelemetry.kotlin.encode

internal interface OtlpJsonEncoder<in T> {
    fun encode(value: T): Sequence<String>
}