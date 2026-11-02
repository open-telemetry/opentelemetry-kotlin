package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SimpleSpanProcessorConfigDslImpTest {
    @Test
    fun startsUnset() {
        val dsl = SimpleSpanProcessorConfigDslImpl()
        assertEquals(
            SimpleSpanProcessorBehavior(),
            dsl.toBehavior()
        )
    }

    @Test
    fun mapsAllFields() {
        val dsl = SimpleSpanProcessorConfigDslImpl().apply {
            exporter {
                console()
                http {
                    endpoint = "http://www.example.com:8080/logs"
                    timeout = 1_000L
                    headers = mapOf("key" to "value")
                }
            }
        }
        assertEquals(
            SimpleSpanProcessorBehavior(
                exporter = SpanExporterBehavior(
                    console = ConsoleExporterBehavior(),
                    http = OtlpHttpSpanExporterBehavior(
                        endpoint = "http://www.example.com:8080/logs",
                        timeout = 1_000L,
                        headers = mapOf("key" to "value")
                    ),
                ),
            ),
            dsl.toBehavior()
        )
    }
}
