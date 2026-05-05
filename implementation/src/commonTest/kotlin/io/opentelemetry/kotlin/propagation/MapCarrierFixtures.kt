package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal object MapTextMapGetter : TextMapGetter<Map<String, String>> {
    override fun keys(carrier: Map<String, String>): Collection<String> = carrier.keys
    override fun get(carrier: Map<String, String>, key: String): String? = carrier[key]
    override fun getAll(carrier: Map<String, String>, key: String): List<String> =
        carrier[key]?.let { listOf(it) } ?: emptyList()
}

@OptIn(ExperimentalApi::class)
internal object MapTextMapSetter : TextMapSetter<MutableMap<String, String>> {
    override fun set(carrier: MutableMap<String, String>, key: String, value: String) {
        carrier[key] = value
    }
}
