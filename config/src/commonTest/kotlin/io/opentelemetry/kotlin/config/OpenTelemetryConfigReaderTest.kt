package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.envar.EnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class OpenTelemetryConfigReaderTest {

    @Test
    fun `should apply the environment when it is the only mechanism`() {
        val behavior = read(env = mapOf(LOGRECORD_COUNT to "64"))
        assertEquals(64, behavior.logRecordAttributeCountLimit())
    }

    @Test
    fun `should apply the dsl when it is the only mechanism`() {
        val behavior = read(dsl = logAttributeCountLimit(64))
        assertEquals(64, behavior.logRecordAttributeCountLimit())
    }

    @Test
    fun `should leave everything unset when no mechanism configured anything`() {
        assertNull(read().logRecordAttributeCountLimit())
    }

    @Test
    fun `should let the dsl override the environment`() {
        val behavior = read(
            env = mapOf(LOGRECORD_COUNT to "64"),
            dsl = logAttributeCountLimit(8),
        )
        assertEquals(8, behavior.logRecordAttributeCountLimit())
    }

    @Test
    fun `should let a config file replace the environment rather than merge with it`() {
        val behavior = read(
            env = mapOf(LOGRECORD_COUNT to "64"),
            fileContents = logAttributeValueLengthLimit(256),
            configFilePath = PATH,
        )
        assertNull(behavior.logRecordAttributeCountLimit())
        assertEquals(256, behavior.logRecordAttributeValueLengthLimit())
    }

    @Test
    fun `should let the dsl override a config file`() {
        val behavior = read(
            fileContents = logAttributeCountLimit(64),
            configFilePath = PATH,
            dsl = logAttributeCountLimit(8),
        )
        assertEquals(8, behavior.logRecordAttributeCountLimit())
    }

    @Test
    fun `should read the config file path from the environment`() {
        val behavior = read(
            env = mapOf(CONFIG_FILE to PATH),
            fileContents = logAttributeCountLimit(64),
        )
        assertEquals(64, behavior.logRecordAttributeCountLimit())
    }

    @Test
    fun `should let an explicit config file path override the environment`() {
        var requested: String? = null
        read(
            env = mapOf(CONFIG_FILE to "from-env.yaml"),
            fileContents = logAttributeCountLimit(64),
            configFilePath = PATH,
            onRead = { requested = it },
        )
        assertEquals(PATH, requested)
    }

    @Test
    fun `should not read a config file when no path was supplied`() {
        var read = false
        read(
            env = mapOf(LOGRECORD_COUNT to "64"),
            onRead = { read = true },
        )
        assertTrue(!read, "no path was supplied, so no file should have been read")
    }

    @Test
    fun `should not read a config file on platforms that do not support them`() {
        val reader = OpenTelemetryConfigReader(
            envVarReader = EnvVarReader(mapOf(CONFIG_FILE to PATH, LOGRECORD_COUNT to "64")::get),
            declarativeConfigReader = null,
        )
        val behavior = reader.read(configFilePath = PATH)
        assertEquals(64, behavior.logRecordAttributeCountLimit())
    }

    @Test
    fun `should propagate a failure to read the config file`() {
        assertFailsWith<IllegalStateException> {
            read(
                configFilePath = PATH,
                onRead = { error("cannot read $it") },
            )
        }
    }

    private fun read(
        env: Map<String, String> = emptyMap(),
        dsl: OpenTelemetryBehavior? = null,
        fileContents: OpenTelemetryBehavior = OpenTelemetryBehavior(),
        configFilePath: String? = null,
        onRead: (String) -> Unit = {},
    ): OpenTelemetryBehavior {
        val reader = OpenTelemetryConfigReader(
            envVarReader = EnvVarReader(env::get),
            declarativeConfigReader = { path ->
                onRead(path)
                fileContents
            },
        )
        return reader.read(dsl = dsl, configFilePath = configFilePath)
    }

    private fun OpenTelemetryBehavior.logRecordAttributeCountLimit() =
        loggerProvider?.logLimits?.attributeCountLimit

    private fun OpenTelemetryBehavior.logRecordAttributeValueLengthLimit() =
        loggerProvider?.logLimits?.attributeValueLengthLimit

    private fun logAttributeCountLimit(limit: Int) = OpenTelemetryBehavior(
        loggerProvider = LoggerProviderBehavior(
            logLimits = LogLimitsBehavior(attributeCountLimit = limit),
        ),
    )

    private fun logAttributeValueLengthLimit(limit: Int) = OpenTelemetryBehavior(
        loggerProvider = LoggerProviderBehavior(
            logLimits = LogLimitsBehavior(attributeValueLengthLimit = limit),
        ),
    )

    private companion object {
        const val PATH = "config.yaml"
        const val CONFIG_FILE = "OTEL_CONFIG_FILE"
        const val LOGRECORD_COUNT = "OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT"
    }
}
