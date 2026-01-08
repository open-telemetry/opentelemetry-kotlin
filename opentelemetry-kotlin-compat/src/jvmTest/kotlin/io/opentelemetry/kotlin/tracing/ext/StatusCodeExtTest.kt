package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaStatusCode
import io.opentelemetry.kotlin.tracing.StatusCode
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class StatusCodeExtTest {

    @Test
    fun toOtelJavaStatusCode() {
        val expected = mapOf(
            StatusCode.UNSET to OtelJavaStatusCode.UNSET,
            StatusCode.ERROR to OtelJavaStatusCode.ERROR,
            StatusCode.OK to OtelJavaStatusCode.OK,
        )
        expected.forEach {
            assertEquals(it.key.toOtelJavaStatusCode(), it.value)
        }
    }
}
