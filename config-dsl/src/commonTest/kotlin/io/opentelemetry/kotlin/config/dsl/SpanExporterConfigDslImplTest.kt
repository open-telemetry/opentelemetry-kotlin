package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SpanExporterConfigDslImplTest {
    @Test
    fun startsUnset() {
        val dsl = SpanExporterConfigDslImpl()
        assertEquals(
            SpanExporterBehavior(),
            dsl.toBehavior()
        )
    }

    @Test
    fun mapsAllFields() {
        val dsl = SpanExporterConfigDslImpl().apply {
            console()
            http {
                endpoint = "http://www.example.com:8080/logs"
                timeout = 1_000L
                headers = mapOf("key" to "value")
            }
        }
        assertEquals(
            SpanExporterBehavior(
                console = ConsoleExporterBehavior(),
                http = OtlpHttpSpanExporterBehavior(
                    endpoint = "http://www.example.com:8080/logs",
                    timeout = 1_000L,
                    headers = mapOf("key" to "value")
                )
            ),
            dsl.toBehavior()
        )
    }
}
