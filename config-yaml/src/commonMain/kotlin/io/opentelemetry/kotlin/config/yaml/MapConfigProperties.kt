package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.ConfigProperties

/**
 * A [ConfigProperties] view over a raw mapping node produced by [YamlConfigParser].
 *
 * Getters return `null` when a property is absent, set to `null`, or not of the requested type.
 * Any scalar can be read as a string, any number as a double, and a whole number as a long; no
 * other conversions are made. A sequence is only returned if every element has the requested type.
 *
 * A property set to `null` is still listed in [propertyKeys], which is how callers tell it apart
 * from a property that is not set.
 */
@ExperimentalApi
internal class MapConfigProperties(private val node: Map<String, Any?>) : ConfigProperties {

    override fun getString(name: String): String? = asString(node[name])

    override fun getBoolean(name: String): Boolean? = node[name] as? Boolean

    override fun getLong(name: String): Long? = asLong(node[name])

    override fun getDouble(name: String): Double? = asDouble(node[name])

    override fun getStringList(name: String): List<String>? = getList(name, ::asString)

    override fun getBooleanList(name: String): List<Boolean>? = getList(name) { it as? Boolean }

    override fun getLongList(name: String): List<Long>? = getList(name, ::asLong)

    override fun getDoubleList(name: String): List<Double>? = getList(name, ::asDouble)

    override fun getStructured(name: String): ConfigProperties? = asStructured(node[name])

    override fun getStructuredList(name: String): List<ConfigProperties>? = getList(name, ::asStructured)

    override val propertyKeys: Set<String>
        get() = node.keys

    private inline fun <T : Any> getList(name: String, convert: (Any?) -> T?): List<T>? {
        val list = node[name] as? List<*> ?: return null
        return list.map { convert(it) ?: return null }
    }

    // yamlkt does not reliably keep a quoted scalar as a string, so its text is used instead.
    private fun asString(value: Any?): String? = when (value) {
        is String -> value
        is Number, is Boolean -> value.toString()
        else -> null
    }

    // Kotlin/JS cannot tell number types apart, so whole numbers are detected by value.
    private fun asLong(value: Any?): Long? = when (value) {
        is Long -> value
        is Number -> value.toDouble().takeIf { it % 1.0 == 0.0 }?.toLong()
        else -> null
    }

    private fun asDouble(value: Any?): Double? = (value as? Number)?.toDouble()

    private fun asStructured(value: Any?): ConfigProperties? =
        (value as? Map<*, *>)?.toStringKeyedMap()?.let(::MapConfigProperties)
}

@ExperimentalApi
private val EMPTY_CONFIG_PROPERTIES: ConfigProperties = MapConfigProperties(emptyMap())

/**
 * Returns a [ConfigProperties] for this mapping node, or an empty one if the node is absent.
 */
@ExperimentalApi
fun Map<String, Any?>?.toConfigProperties(): ConfigProperties =
    this?.let(::MapConfigProperties) ?: EMPTY_CONFIG_PROPERTIES

/**
 * Returns the entries of this map whose key is a [String], as YAML mapping keys need not be strings.
 */
internal fun Map<*, *>.toStringKeyedMap(): Map<String, Any?> =
    entries.mapNotNull { (key, value) -> (key as? String)?.let { it to value } }.toMap()
