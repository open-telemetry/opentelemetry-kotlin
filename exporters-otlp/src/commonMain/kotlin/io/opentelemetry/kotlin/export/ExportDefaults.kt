package io.opentelemetry.kotlin.export

internal const val EXPORT_INITIAL_DELAY_MS: Long = 30_000L // 30s
internal const val EXPORT_MAX_ATTEMPT_INTERVAL_MS: Long = 600000L // 10 mins
internal const val EXPORT_MAX_ATTEMPTS: Int = 8 // maximum of 8 retries per export call
