package io.opentelemetry.kotlin

/**
 * Marks declarations that are public only so other opentelemetry-kotlin modules can use them. They are not part of
 * the supported API and may change or be removed in any release without notice.
 */
@RequiresOptIn(
    message = "This is an internal opentelemetry-kotlin API and may change or be removed without notice.",
    level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.TYPEALIAS)
public annotation class InternalApi
