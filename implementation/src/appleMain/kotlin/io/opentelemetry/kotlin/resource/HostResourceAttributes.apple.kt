package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.OsAttributes
import platform.UIKit.UIDevice

@OptIn(IncubatingApi::class)
internal actual fun detectHostResourceAttributes(): HostResourceAttributes {
    val device = UIDevice.currentDevice

    return HostResourceAttributes(
        osType = OsAttributes.OsTypeValues.DARWIN.value,
        osName = device.systemName.takeIf(String::isNotBlank),
        osVersion = device.systemVersion.takeIf(String::isNotBlank),
    )
}
