package io.opentelemetry.kotlin.resource

import android.os.Build

internal actual fun detectDeviceResourceAttributes(): DeviceResourceAttributes = DeviceResourceAttributes(
    manufacturer = Build.MANUFACTURER.takeIf(String::isNotBlank),
    modelIdentifier = Build.DEVICE.takeIf(String::isNotBlank),
    modelName = Build.MODEL.takeIf(String::isNotBlank),
)
