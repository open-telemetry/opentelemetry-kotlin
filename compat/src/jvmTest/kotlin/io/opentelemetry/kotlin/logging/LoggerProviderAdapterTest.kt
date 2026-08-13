package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import kotlin.test.Test
import kotlin.test.assertNotSame

internal class LoggerProviderAdapterTest {

    private val adapter = LoggerProviderAdapter(OtelJavaSdkLoggerProvider.builder().build())

    @Test
    fun testScopePropertyBoundaryCollision() {
        val first = adapter.getLogger(name = "ab", version = "c")
        val second = adapter.getLogger(name = "a", version = "bc")
        assertNotSame(first, second)
    }

    @Test
    fun testNullScopePropertyCollision() {
        val first = adapter.getLogger(name = "name")
        val second = adapter.getLogger(name = "name", version = "null")
        assertNotSame(first, second)
    }
}
