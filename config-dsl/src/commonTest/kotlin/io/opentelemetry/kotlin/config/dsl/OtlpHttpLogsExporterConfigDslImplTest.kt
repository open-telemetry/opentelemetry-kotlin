package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpHttpLogsExporterConfigDslImplTest {
    @Test
    fun startsUnset() {
        val dsl = OtlpHttpLogsExporterConfigDslImpl()
        assertEquals(
            OtlpHttpLogsExporterBehavior(),
            dsl.toBehavior()
        )
    }

    @Test
    fun mapsAllFields() {
        val dsl = OtlpHttpLogsExporterConfigDslImpl().apply {
            endpoint = "http://www.example.com:8080/logs"
            timeout = 1_000L
            headers = mapOf("key" to "value")
        }
        assertEquals(
            OtlpHttpLogsExporterBehavior(
                endpoint = "http://www.example.com:8080/logs",
                timeout = 1_000L,
                headers = mapOf("key" to "value")
            ),
            dsl.toBehavior()
        )
    }
}
