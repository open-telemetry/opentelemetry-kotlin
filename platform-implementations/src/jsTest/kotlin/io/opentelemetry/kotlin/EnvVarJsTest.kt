package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals

internal class EnvVarJsTest {

    @Test
    fun testEnvVarReadFromProcessEnv() {
        val env = js("process.env")
        env["OTEL_KOTLIN_TEST_ENV_VAR"] = "42"
        assertEquals("42", getEnvVarValue("OTEL_KOTLIN_TEST_ENV_VAR"))
    }
}
