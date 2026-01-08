package io.opentelemetry.kotlin

/**
 * Gets the 'type' of the given exception or returns null if this cannot be found. For example,
 * on the JVM this would typically be the fully qualified class name, e.g. java.lang.IllegalArgumentException
 */
public expect fun Throwable.exceptionType(): String?
