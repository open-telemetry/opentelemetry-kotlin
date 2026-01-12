package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
fun Map<String, Any>.toSerializable(): Map<String, String> = mapValues {
    it.value.toString()
}
