package io.opentelemetry.kotlin.framework.serialization.conversion
fun Map<String, Any>.toSerializable(): Map<String, String> = mapValues {
    it.value.toString()
}
