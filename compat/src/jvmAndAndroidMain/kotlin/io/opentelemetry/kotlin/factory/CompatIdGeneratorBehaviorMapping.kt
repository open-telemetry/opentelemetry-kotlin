package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior

internal fun IdGeneratorBehavior.toIdGenerator(): IdGenerator = when (this) {
    IdGeneratorBehavior.Random -> CompatIdGenerator()
}
