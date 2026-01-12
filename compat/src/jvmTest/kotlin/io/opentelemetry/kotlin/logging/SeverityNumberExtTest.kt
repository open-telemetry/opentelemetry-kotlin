package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import org.junit.Test

@OptIn(ExperimentalApi::class)
internal class SeverityNumberExtTest {

    @Test
    fun `test toOtelJavaSeverityNumber`() {
        val map = mapOf(
            SeverityNumber.UNKNOWN to OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER,
            SeverityNumber.TRACE to OtelJavaSeverity.TRACE,
            SeverityNumber.TRACE2 to OtelJavaSeverity.TRACE2,
            SeverityNumber.TRACE3 to OtelJavaSeverity.TRACE3,
            SeverityNumber.TRACE4 to OtelJavaSeverity.TRACE4,
            SeverityNumber.DEBUG to OtelJavaSeverity.DEBUG,
            SeverityNumber.DEBUG2 to OtelJavaSeverity.DEBUG2,
            SeverityNumber.DEBUG3 to OtelJavaSeverity.DEBUG3,
            SeverityNumber.DEBUG4 to OtelJavaSeverity.DEBUG4,
            SeverityNumber.INFO to OtelJavaSeverity.INFO,
            SeverityNumber.INFO2 to OtelJavaSeverity.INFO2,
            SeverityNumber.INFO3 to OtelJavaSeverity.INFO3,
            SeverityNumber.INFO4 to OtelJavaSeverity.INFO4,
            SeverityNumber.WARN to OtelJavaSeverity.WARN,
            SeverityNumber.WARN2 to OtelJavaSeverity.WARN2,
            SeverityNumber.WARN3 to OtelJavaSeverity.WARN3,
            SeverityNumber.WARN4 to OtelJavaSeverity.WARN4,
            SeverityNumber.ERROR to OtelJavaSeverity.ERROR,
            SeverityNumber.ERROR2 to OtelJavaSeverity.ERROR2,
            SeverityNumber.ERROR3 to OtelJavaSeverity.ERROR3,
            SeverityNumber.ERROR4 to OtelJavaSeverity.ERROR4,
            SeverityNumber.FATAL to OtelJavaSeverity.FATAL,
            SeverityNumber.FATAL2 to OtelJavaSeverity.FATAL2,
            SeverityNumber.FATAL3 to OtelJavaSeverity.FATAL3,
            SeverityNumber.FATAL4 to OtelJavaSeverity.FATAL4,
        )

        map.forEach {
            assert(it.key.toOtelJavaSeverityNumber() == it.value)
        }
    }

    @Test
    fun `test toOtelKotlinSeverityNumber`() {
        val map = mapOf(
            OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER to SeverityNumber.UNKNOWN,
            OtelJavaSeverity.TRACE to SeverityNumber.TRACE,
            OtelJavaSeverity.TRACE2 to SeverityNumber.TRACE2,
            OtelJavaSeverity.TRACE3 to SeverityNumber.TRACE3,
            OtelJavaSeverity.TRACE4 to SeverityNumber.TRACE4,
            OtelJavaSeverity.DEBUG to SeverityNumber.DEBUG,
            OtelJavaSeverity.DEBUG2 to SeverityNumber.DEBUG2,
            OtelJavaSeverity.DEBUG3 to SeverityNumber.DEBUG3,
            OtelJavaSeverity.DEBUG4 to SeverityNumber.DEBUG4,
            OtelJavaSeverity.INFO to SeverityNumber.INFO,
            OtelJavaSeverity.INFO2 to SeverityNumber.INFO2,
            OtelJavaSeverity.INFO3 to SeverityNumber.INFO3,
            OtelJavaSeverity.INFO4 to SeverityNumber.INFO4,
            OtelJavaSeverity.WARN to SeverityNumber.WARN,
            OtelJavaSeverity.WARN2 to SeverityNumber.WARN2,
            OtelJavaSeverity.WARN3 to SeverityNumber.WARN3,
            OtelJavaSeverity.WARN4 to SeverityNumber.WARN4,
            OtelJavaSeverity.ERROR to SeverityNumber.ERROR,
            OtelJavaSeverity.ERROR2 to SeverityNumber.ERROR2,
            OtelJavaSeverity.ERROR3 to SeverityNumber.ERROR3,
            OtelJavaSeverity.ERROR4 to SeverityNumber.ERROR4,
            OtelJavaSeverity.FATAL to SeverityNumber.FATAL,
            OtelJavaSeverity.FATAL2 to SeverityNumber.FATAL2,
            OtelJavaSeverity.FATAL3 to SeverityNumber.FATAL3,
            OtelJavaSeverity.FATAL4 to SeverityNumber.FATAL4,
        )

        map.forEach {
            assert(it.key.toOtelKotlinSeverityNumber() == it.value)
        }
    }
}
