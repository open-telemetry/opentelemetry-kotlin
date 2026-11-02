package io.opentelemetry.kotlin

data class InstrumentationScopeInfoImpl(
    override val name: String,
    override val version: String?,
    override val schemaUrl: String?,
    override val attributes: Map<String, Any>
) : InstrumentationScopeInfo {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is InstrumentationScopeInfoImpl) {
            return false
        }
        return name == other.name &&
            version == other.version &&
            schemaUrl == other.schemaUrl &&
            attributesEqual(attributes, other.attributes)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + (schemaUrl?.hashCode() ?: 0)
        result = 31 * result + attributesHashCode(attributes)
        return result
    }

    private fun attributesEqual(left: Map<String, Any>, right: Map<String, Any>): Boolean {
        if (left.size != right.size) {
            return false
        }
        for ((key, value) in left) {
            val otherValue = right[key] ?: return false
            if (value is ByteArray && otherValue is ByteArray) {
                if (!value.contentEquals(otherValue)) {
                    return false
                }
            } else if (value != otherValue) {
                return false
            }
        }
        return true
    }

    private fun attributesHashCode(attributes: Map<String, Any>): Int {
        var result = 0
        for ((key, value) in attributes) {
            val valueHashCode = if (value is ByteArray) {
                value.contentHashCode()
            } else {
                value.hashCode()
            }
            result += key.hashCode() xor valueHashCode
        }
        return result
    }
}
