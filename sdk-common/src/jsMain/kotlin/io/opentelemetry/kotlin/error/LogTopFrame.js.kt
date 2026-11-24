package io.opentelemetry.kotlin.error

internal actual fun Throwable.topFrameForLogDeduplication(): String? = null
