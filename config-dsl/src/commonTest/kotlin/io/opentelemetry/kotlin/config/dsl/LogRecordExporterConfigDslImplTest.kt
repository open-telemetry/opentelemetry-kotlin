package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogRecordExporterConfigDslImplTest {
    @Test
    fun startsUnset() {
        val dsl = LogRecordExporterConfigDslImpl()
        assertEquals(
            LogRecordExporterBehavior(),
            dsl.toBehavior()
        )
    }

    @Test
    fun mapsAllFields() {
        val dsl = LogRecordExporterConfigDslImpl().apply {
            http {
                endpoint = "http://www.example.com"
            }
            console()
        }
        assertEquals(
            LogRecordExporterBehavior(
                http = OtlpHttpLogsExporterBehavior(
                    endpoint = "http://www.example.com"
                ),
                console = ConsoleExporterBehavior()
            ),
            dsl.toBehavior()
        )
    }
}
