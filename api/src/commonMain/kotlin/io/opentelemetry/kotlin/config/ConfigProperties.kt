package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Represents a node in the declarative configuration tree.
 *
 * Accessors return `null` when the named property is absent or its value cannot be converted
 * to the requested type.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/#declarative-configuration
 */
@ExperimentalApi
@ThreadSafe
public interface ConfigProperties {

    /**
     * Returns a string value for [name] or null if it doesn't exist.
     */
    public fun getString(name: String): String?

    /**
     * Returns a boolean value for [name] or null if it doesn't exist.
     */
    public fun getBoolean(name: String): Boolean?

    /**
     * Returns a long value for [name] or null if it doesn't exist.
     */
    public fun getLong(name: String): Long?

    /**
     * Returns a double value for [name] or null if it doesn't exist.
     */
    public fun getDouble(name: String): Double?

    /**
     * Returns a string list value for [name] or null if it doesn't exist.
     */
    public fun getStringList(name: String): List<String>?

    /**
     * Returns a boolean list value for [name] or null if it doesn't exist.
     */
    public fun getBooleanList(name: String): List<Boolean>?

    /**
     * Returns a long list value for [name] or null if it doesn't exist.
     */
    public fun getLongList(name: String): List<Long>?

    /**
     * Returns a double list value for [name] or null if it doesn't exist.
     */
    public fun getDoubleList(name: String): List<Double>?

    /**
     * Returns the child mapping node identified by [name], or `null` if absent.
     */
    public fun getStructured(name: String): ConfigProperties?

    /**
     * Returns the list of child mapping nodes identified by [name], or `null` if absent.
     */
    public fun getStructuredList(name: String): List<ConfigProperties>?

    /**
     * The names of all properties present in this mapping node.
     */
    public val propertyKeys: Set<String>
}
