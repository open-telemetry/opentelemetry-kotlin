package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefault

internal fun IdGeneratorBehavior.toIdGenerator(sdkErrorHandler: SdkErrorHandler): IdGenerator = when (this) {
    IdGeneratorBehavior.Random -> CompatIdGenerator()
    is IdGeneratorBehavior.Custom ->
        sdkErrorHandler.guardOrDefault(null, "Failed to create IdGenerator, using default") { supplier() }
            ?: CompatIdGenerator()
}
