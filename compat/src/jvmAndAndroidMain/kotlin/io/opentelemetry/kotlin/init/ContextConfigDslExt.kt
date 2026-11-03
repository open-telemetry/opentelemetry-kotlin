package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.OtelJavaCurrentContextStorage

/**
 * Stores the current context in opentelemetry-java's `ContextStorage`. Enable this if you use
 * [io.opentelemetry.kotlin.toOtelJavaApi], so that the Kotlin and Java APIs share one current
 * context. Takes precedence over [ContextConfigDsl.storageMode].
 */
@ExperimentalApi
public fun ContextConfigDsl.useOtelJavaContextStorage() {
    storage(::OtelJavaCurrentContextStorage)
}
