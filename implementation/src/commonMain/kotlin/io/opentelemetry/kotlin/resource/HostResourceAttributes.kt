package io.opentelemetry.kotlin.resource

internal data class HostResourceAttributes(
    val osType: String? = null,
    val osName: String? = null,
    val osVersion: String? = null,
) {
    val isEmpty: Boolean
        get() = osType == null && osName == null && osVersion == null
}

internal expect fun detectHostResourceAttributes(): HostResourceAttributes
