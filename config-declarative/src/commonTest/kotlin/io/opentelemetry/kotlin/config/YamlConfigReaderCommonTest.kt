package io.opentelemetry.kotlin.config

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

internal class YamlConfigReaderCommonTest {

    @Test
    fun `should be a supported reader`() {
        assertIs<DeclarativeBehaviorReader>(yamlConfigReader())
    }

    @Test
    fun `should throw when the file cannot be read`() {
        val reader = assertIs<DeclarativeBehaviorReader>(yamlConfigReader())
        assertFailsWith<Throwable> { reader.read("does-not-exist.yaml") }
    }
}
