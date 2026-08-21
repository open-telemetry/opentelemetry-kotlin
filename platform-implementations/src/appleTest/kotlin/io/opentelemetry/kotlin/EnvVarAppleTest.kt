package io.opentelemetry.kotlin

import platform.posix.setenv
import kotlin.test.Test
import kotlin.test.assertEquals

internal class EnvVarAppleTest {

    @Test
    fun testEnvVarReadFromPosixEnv() {
        setenv("OTEL_KOTLIN_TEST_ENV_VAR", "42", 1)
        assertEquals("42", getEnvVarValue("OTEL_KOTLIN_TEST_ENV_VAR"))
    }
}
