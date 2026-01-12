package io.opentelemetry.kotlin

import kotlin.js.Date

public actual fun getCurrentTimeNanos(): Long {
    val ms = Date.now().toLong()
    return ms * 1_000_000
}
