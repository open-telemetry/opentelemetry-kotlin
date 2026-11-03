package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.ConfigProperties

/**
 * A [ConfigProperties] view over a raw mapping node produced by [YamlConfigParser].
 *
 * Getters return `null` when a property is absent, set to `null`, or not of the requested type.
 * Any scalar can be read as a string, any number as a double, and a whole number within the range
 * of a long as a long; no other conversions are made. A floating-point value read as a string is
 * formatted by the platform, so `42.0` reads as `"42.0"` on the JVM and `"42"` on JS. A sequence is
 * only returned if every element has the requested type.
 *
 * A property set to `null` is still listed in [propertyKeys], which is how callers tell it apart
 * from a property that is not set.
 */
internal class MapConfigProperties(private val node: Map<String, Any?>) : ConfigProperties {

    override fun getString(name: String): String? = node[name].scalarToString()

    override fun getBoolean(name: String): Boolean? = node[name] as? Boolean

    override fun getLong(name: String): Long? = asLong(node[name])

    override fun getDouble(name: String): Double? = asDouble(node[name])

    override fun getStringList(name: String): List<String>? = getList(name) { it.scalarToString() }

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

    // Kotlin/JS cannot tell number types apart, so whole numbers are detected by value.
    private fun asLong(value: Any?): Long? = (value as? Number)?.toLongIfWhole()

    private fun asDouble(value: Any?): Double? = (value as? Number)?.toDouble()

    private fun asStructured(value: Any?): ConfigProperties? =
        (value as? Map<*, *>)?.toStringKeyedMap()?.let(::MapConfigProperties)
}

private val EMPTY_CONFIG_PROPERTIES: ConfigProperties = MapConfigProperties(emptyMap())

/**
 * Returns a [ConfigProperties] for a copy of this mapping node, or an empty one if the node is absent.
 */
@ExperimentalApi
fun Map<String, Any?>?.toConfigProperties(): ConfigProperties =
    this?.let { MapConfigProperties(it.toMap()) } ?: EMPTY_CONFIG_PROPERTIES

/**
 * Returns a copy of this map keyed by strings. YAML mapping keys can be any scalar, so scalar keys
 * are converted to their text, and entries keyed by a mapping, sequence or `null` are dropped.
 */
internal fun Map<*, *>.toStringKeyedMap(): Map<String, Any?> =
    entries.mapNotNull { (key, value) -> key.scalarToString()?.let { it to value } }.toMap()

// yamlkt does not reliably keep a quoted scalar as a string, so its text is used instead.
private fun Any?.scalarToString(): String? = when (this) {
    is String -> this
    is Number, is Boolean -> toString()
    else -> null
}
