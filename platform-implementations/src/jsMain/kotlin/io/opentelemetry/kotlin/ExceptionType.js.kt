package io.opentelemetry.kotlin

public actual fun Throwable.exceptionType(): String? = this::class.simpleName
