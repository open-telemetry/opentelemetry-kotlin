package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Returns a shared, stateless getter for single-value maps, with case-insensitive key lookup.
 * When keys differ only in case, [TextMapGetter.get] returns the first matching value
 * and [TextMapGetter.getAll] returns all matching values in iteration order.
 */
@ExperimentalApi
public fun createMapTextMapGetter(): TextMapGetter<Map<String, String>> = MapTextMapGetter

/**
 * Returns a shared, stateless setter for mutable single-value maps.
 * Keys are stored as supplied, using the map's own key equality for replacement.
 */
@ExperimentalApi
public fun createMutableMapTextMapSetter(): TextMapSetter<MutableMap<String, String>> = MutableMapTextMapSetter

@OptIn(ExperimentalApi::class)
internal object MapTextMapGetter : TextMapGetter<Map<String, String>> {
    override fun keys(carrier: Map<String, String>): Collection<String> = carrier.keys

    override fun get(carrier: Map<String, String>, key: String): String? =
        carrier.entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value

    override fun getAll(carrier: Map<String, String>, key: String): List<String> =
        carrier.entries.filter { it.key.equals(key, ignoreCase = true) }.map { it.value }
}

@OptIn(ExperimentalApi::class)
internal object MutableMapTextMapSetter : TextMapSetter<MutableMap<String, String>> {
    override fun set(carrier: MutableMap<String, String>, key: String, value: String) {
        carrier[key] = value
    }
}
