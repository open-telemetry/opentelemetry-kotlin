package io.opentelemetry.kotlin.config.envar.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

internal class EnvVarNameTest {
    @Test
    fun `should successfully create an env var name`() {
        // given
        val envVar = "OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT"

        // when
        val envVarName = EnvVarName(envVar)

        // then
        assertEquals("OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT", envVarName.value)
    }

    @Test
    fun `should throw an error when creating an invalid env var name`() {
        // given
        val envVar = "invalid-env-var"

        // when
        val result = assertFails {
            EnvVarName(envVar)
        }


        // then
        assertIs<IllegalArgumentException>(result)
    }
}
