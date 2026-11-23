package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class EnvVarJvmTest {

    @Test
    fun testEnvVarReadFromSystemEnv() {
        val expected = assertNotNull(System.getenv("PATH"))
        assertEquals(expected, getEnvVarValue("PATH"))
    }

    @Test
    fun testSupportsEnvVars() {
        assertTrue(supportsEnvVars)
    }
}
