package io.opentelemetry.kotlin.resource

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.UIKit.UIDevice
import platform.posix.getenv
import platform.posix.uname
import platform.posix.utsname

internal actual fun detectDeviceResourceAttributes(): DeviceResourceAttributes = DeviceResourceAttributes(
    manufacturer = "Apple",
    modelIdentifier = detectAppleDeviceModelIdentifier(),
    modelName = UIDevice.currentDevice.model.takeIf(String::isNotBlank),
)

private fun detectAppleDeviceModelIdentifier(): String? =
    getSimulatorModelIdentifier() ?: getMachineModelIdentifier()

@OptIn(ExperimentalForeignApi::class)
private fun getSimulatorModelIdentifier(): String? =
    getenv("SIMULATOR_MODEL_IDENTIFIER")
        ?.toKString()
        ?.takeIf(String::isNotBlank)

@OptIn(ExperimentalForeignApi::class)
private fun getMachineModelIdentifier(): String? =
    memScoped {
        val systemInfo = alloc<utsname>()
        if (uname(systemInfo.ptr) != 0) {
            return@memScoped null
        }

        systemInfo.machine.toKString().takeIf(String::isNotBlank)
    }
