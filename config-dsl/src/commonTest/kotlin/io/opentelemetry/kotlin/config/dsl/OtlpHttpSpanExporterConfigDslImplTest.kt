package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpHttpSpanExporterConfigDslImplTest {
    @Test
    fun startsUnset() {
        val dsl = OtlpHttpSpanExporterConfigDslImpl()
        assertEquals(
            OtlpHttpSpanExporterBehavior(),
            dsl.toBehavior()
        )
    }

    @Test
    fun mapsAllFields() {
        val dsl = OtlpHttpSpanExporterConfigDslImpl().apply {
            endpoint = "http://www.example.com:8080/logs"
            timeout = 1_000L
            headers = mapOf("key" to "value")
        }
        assertEquals(
            OtlpHttpSpanExporterBehavior(
                endpoint = "http://www.example.com:8080/logs",
                timeout = 1_000L,
                headers = mapOf("key" to "value")
            ),
            dsl.toBehavior()
        )
    }
}
