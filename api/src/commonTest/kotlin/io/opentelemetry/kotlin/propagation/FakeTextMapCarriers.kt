package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Map carriers for :api tests, which can't depend on :test-fakes.
 */
@OptIn(ExperimentalApi::class)
internal object FakeTextMapGetter : TextMapGetter<Map<String, String>> {
    override fun keys(carrier: Map<String, String>): Collection<String> = carrier.keys
    override fun get(carrier: Map<String, String>, key: String): String? = carrier[key]
    override fun getAll(carrier: Map<String, String>, key: String): List<String> =
        carrier[key]?.let { listOf(it) } ?: emptyList()
}

@OptIn(ExperimentalApi::class)
internal object MultiValueMapTextMapGetter : TextMapGetter<Map<String, List<String>>> {
    override fun keys(carrier: Map<String, List<String>>): Collection<String> = carrier.keys
    override fun get(carrier: Map<String, List<String>>, key: String): String? =
        carrier[key]?.firstOrNull()
    override fun getAll(carrier: Map<String, List<String>>, key: String): List<String> =
        carrier[key].orEmpty()
}

@OptIn(ExperimentalApi::class)
internal object FakeTextMapSetter : TextMapSetter<MutableMap<String, String>> {
    override fun set(carrier: MutableMap<String, String>, key: String, value: String) {
        carrier[key] = value
    }
}
