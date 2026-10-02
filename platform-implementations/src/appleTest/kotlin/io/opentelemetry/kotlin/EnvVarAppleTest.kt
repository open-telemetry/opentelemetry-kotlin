package io.opentelemetry.kotlin

import platform.posix.setenv
import kotlin.test.Test
import kotlin.test.assertNull

internal class EnvVarAppleTest {

    @Test
    fun testEnvVarIgnoredOnApple() {
        setenv("OTEL_KOTLIN_TEST_ENV_VAR", "42", 1)
        assertNull(getEnvVarValue("OTEL_KOTLIN_TEST_ENV_VAR"))
    }
}
