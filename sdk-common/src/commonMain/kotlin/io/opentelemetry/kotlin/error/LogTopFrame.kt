package io.opentelemetry.kotlin.error

/** Returns the first exception frame, if available for this platform. */
internal expect fun Throwable.topFrameForLogDeduplication(): String?
