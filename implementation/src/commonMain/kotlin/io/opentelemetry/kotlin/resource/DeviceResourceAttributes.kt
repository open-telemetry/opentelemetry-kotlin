package io.opentelemetry.kotlin.resource

internal data class DeviceResourceAttributes(
    val manufacturer: String? = null,
    val modelIdentifier: String? = null,
    val modelName: String? = null,
) {
    val isEmpty: Boolean
        get() = manufacturer == null && modelIdentifier == null && modelName == null
}

internal expect fun detectDeviceResourceAttributes(): DeviceResourceAttributes
