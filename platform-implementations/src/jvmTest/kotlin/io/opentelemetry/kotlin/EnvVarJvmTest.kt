package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class EnvVarJvmTest {

    @Test
    fun testEnvVarReadFromSystemEnv() {
        val expected = assertNotNull(System.getenv("PATH"))
        assertEquals(expected, getEnvVarValue("PATH"))
    }
}
