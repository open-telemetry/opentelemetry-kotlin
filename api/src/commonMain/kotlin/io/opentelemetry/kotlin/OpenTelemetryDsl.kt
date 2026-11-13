package io.opentelemetry.kotlin

/**
 * Marks a type as a receiver in the OpenTelemetry API DSL. This helps disambiguate what symbols should be accessible
 * as _implicit_ references within nested lambdas. It is still possible to reference the outer lambda explicitly
 * via this@outerLambdaName.
 *
 * https://kotlinlang.org/docs/type-safe-builders.html#scope-control-dslmarker
 */
@DslMarker
public annotation class OpenTelemetryDsl
