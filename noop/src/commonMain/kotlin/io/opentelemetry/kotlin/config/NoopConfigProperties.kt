package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
internal object NoopConfigProperties : ConfigProperties {
    override fun getString(name: String): String? = null
    override fun getBoolean(name: String): Boolean? = null
    override fun getLong(name: String): Long? = null
    override fun getDouble(name: String): Double? = null
    override fun getStringList(name: String): List<String>? = null
    override fun getBooleanList(name: String): List<Boolean>? = null
    override fun getLongList(name: String): List<Long>? = null
    override fun getDoubleList(name: String): List<Double>? = null
    override fun getStructured(name: String): ConfigProperties? = null
    override fun getStructuredList(name: String): List<ConfigProperties>? = null
    override val propertyKeys: Set<String> = emptySet()
}
