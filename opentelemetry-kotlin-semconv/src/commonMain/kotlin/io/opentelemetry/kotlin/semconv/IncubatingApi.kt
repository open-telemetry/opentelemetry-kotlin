package io.opentelemetry.kotlin.semconv

/**
 * Marks semantic conventions that are incubating and therefore subject to breaking change without
 * any warning between versions.
 */
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS
)
annotation class IncubatingApi
