package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Marks a class as part of the Config API DSL. This helps disambiguate what symbols should be accessible
 * as _implicit_ references within nested lambdas. It is still possible to reference the outer lambda explicitly
 * via this@outerLambdaName.
 *
 * https://kotlinlang.org/docs/type-safe-builders.html#scope-control-dslmarker
 */
@ExperimentalApi
@DslMarker
public annotation class ConfigDsl
