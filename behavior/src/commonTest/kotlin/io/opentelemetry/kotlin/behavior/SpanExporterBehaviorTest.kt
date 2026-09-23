package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertEquals

internal class SpanExporterBehaviorTest {

    @Test
    fun consoleIsSingleton() {
        assertSame(SpanExporterBehavior.Console, SpanExporterBehavior.Console)
    }

    @Test
    fun consoleMergesToHigher() {
        assertSame(SpanExporterBehavior.Console, SpanExporterBehavior.Console.mergeWith(SpanExporterBehavior.Console))
    }

    @Test
    fun otlpHttpCanBeConstructedWithDefaults() {
        val otlp = SpanExporterBehavior.OtlpHttp()
        assertEquals(null, otlp.endpoint)
        assertEquals(null, otlp.timeout)
    }

    @Test
    fun otlpHttpPreservesEndpointAndTimeout() {
        val otlp = SpanExporterBehavior.OtlpHttp(endpoint = "https://example.com", timeout = 5000)
        assertEquals("https://example.com", otlp.endpoint)
        assertEquals(5000, otlp.timeout)
    }

    @Test
    fun otlpHttpMergesToHigherOtlpHttp() {
        val lower = SpanExporterBehavior.OtlpHttp(endpoint = "https://lower.com", timeout = 1000)
        val higher = SpanExporterBehavior.OtlpHttp(endpoint = "https://higher.com", timeout = 2000)
        val result = lower.mergeWith(higher)
        assertSame(higher, result)
    }

    @Test
    fun otlpHttpMergesWithConsoleReturnsOtlpHttp() {
        val otlp = SpanExporterBehavior.OtlpHttp(endpoint = "https://example.com", timeout = 5000)
        val result = otlp.mergeWith(SpanExporterBehavior.Console)
        assertSame(otlp, result)
    }

    @Test
    fun consoleMergesWithOtlpHttpReturnsConsole() {
        val otlp = SpanExporterBehavior.OtlpHttp(endpoint = "https://example.com", timeout = 5000)
        val result = SpanExporterBehavior.Console.mergeWith(otlp)
        assertSame(otlp, result)
    }
}
