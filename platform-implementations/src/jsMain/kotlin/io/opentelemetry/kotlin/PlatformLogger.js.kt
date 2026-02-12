package io.opentelemetry.kotlin

public actual fun platformLog(message: String) {
    console.log(message)
}
