package io.opentelemetry.kotlin

/**
 * Marks APIs that are experimental and therefore subject to breaking change without any warning between versions.
 */
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
public annotation class ExperimentalApi
