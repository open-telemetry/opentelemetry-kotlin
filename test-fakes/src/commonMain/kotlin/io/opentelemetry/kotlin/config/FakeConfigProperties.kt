package io.opentelemetry.kotlin.config

class FakeConfigProperties(val map: MutableMap<String, Any?> = mutableMapOf()) : ConfigProperties {

    override fun getString(name: String): String? = map[name] as? String

    override fun getBoolean(name: String): Boolean? = map[name] as? Boolean

    override fun getLong(name: String): Long? = map[name] as? Long

    override fun getDouble(name: String): Double? = map[name] as? Double

    override fun getStringList(name: String): List<String>? = getList(name)

    override fun getBooleanList(name: String): List<Boolean>? = getList(name)

    override fun getLongList(name: String): List<Long>? = getList(name)

    override fun getDoubleList(name: String): List<Double>? = getList(name)

    override fun getStructured(name: String): ConfigProperties? = map[name] as? ConfigProperties

    override fun getStructuredList(name: String): List<ConfigProperties>? = getList(name)

    override val propertyKeys: Set<String>
        get() = map.keys

    private inline fun <reified T> getList(name: String): List<T>? {
        val list = map[name] as? List<*> ?: return null
        return list.filterIsInstance<T>().takeIf { it.size == list.size }
    }
}
