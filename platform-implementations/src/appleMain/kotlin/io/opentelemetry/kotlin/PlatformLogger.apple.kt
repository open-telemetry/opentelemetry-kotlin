package io.opentelemetry.kotlin

import platform.Foundation.NSLog

public actual fun platformLog(message: String) {
    NSLog(message)
}
