package io.opentelemetry.kotlin

public actual fun <T> threadSafeList(): MutableList<T> = mutableListOf()
