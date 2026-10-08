package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.behavior.BehaviorResolver
import io.opentelemetry.kotlin.behavior.BehaviorResolverImpl
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReader
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class OpenTelemetryConfigReaderTest {

    @Test
    fun `should apply the environment when it is the only mechanism`() {
        val behavior = read(env = mapOf(LOGRECORD_COUNT to "64"))
        assertEquals(64, behavior.logRecordAttributeCountLimit())
    }

    @Test
    fun `should use the default environment variable reader`() {
        val reader = OpenTelemetryConfigReader(declarativeConfigReader = null)
        val behavior = reader.read(dsl = logAttributeCountLimit(64))
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
    fun `should report unknown sampler via SdkErrorHandler`() {
        val handler = FakeSdkErrorHandler()
        val reader = OpenTelemetryConfigReader(
            envVarReader = EnvVarReader(mapOf("OTEL_TRACES_SAMPLER" to "not_a_sampler")::get),
            declarativeConfigReader = null,
            sdkErrorHandler = handler,
        )
        reader.read()
        assertEquals(1, handler.apiMisuses.size)
        val misuse = handler.apiMisuses.single()
        assertTrue(misuse.message.contains("not_a_sampler"))
        assertEquals("OTEL_TRACES_SAMPLER", misuse.api)
        assertEquals(SdkErrorSeverity.WARNING, misuse.severity)
    }

    @Test
    fun `should treat an unreadable config file as empty`() {
        val handler = FakeSdkErrorHandler()
        val behavior = read(
            env = mapOf(LOGRECORD_COUNT to "64"),
            dsl = logAttributeValueLengthLimit(256),
            configFilePath = PATH,
            onRead = { error("cannot read $it") },
            handler = handler,
        )
        assertNull(behavior.logRecordAttributeCountLimit())
        assertEquals(256, behavior.logRecordAttributeValueLengthLimit())
        val misuse = handler.apiMisuses.single()
        assertEquals("OTEL_CONFIG_FILE", misuse.api)
        assertTrue(misuse.message.contains("cannot read $PATH"))
        assertEquals(SdkErrorSeverity.ERROR, misuse.severity)
        assertEquals(1, handler.errors.size)
    }

    @Test
    fun `should skip malformed header entries from the environment`() {
        val handler = FakeSdkErrorHandler()
        val behavior = read(
            env = mapOf(
                "OTEL_TRACES_EXPORTER" to "otlp",
                "OTEL_EXPORTER_OTLP_HEADERS" to "key=value,malformed",
            ),
            handler = handler,
        )
        assertEquals(mapOf("key" to "value"), behavior.tracerProvider?.processor?.http?.headers)
        assertTrue(handler.sdkCodeErrors.isEmpty())
    }

    @Test
    fun `should treat a failing dsl as unset`() {
        val handler = FakeSdkErrorHandler()
        val behavior = read(
            env = mapOf(LOGRECORD_COUNT to "64"),
            dslProvider = { error("dsl failed") },
            handler = handler,
        )
        assertEquals(64, behavior.logRecordAttributeCountLimit())
        val error = handler.userCodeErrors.single()
        assertEquals("dsl failed", error.cause.message)
        assertEquals(SdkErrorSeverity.ERROR, error.severity)
        assertEquals(1, handler.errors.size)
    }

    @Test
    fun `should treat failing environment variables as unset`() {
        val behavior = read(
            env = mapOf("OTEL_TRACES_SAMPLER" to "not_a_sampler", LOGRECORD_COUNT to "64"),
            dsl = logAttributeValueLengthLimit(256),
            handler = { error("handler failed") },
        )
        assertNull(behavior.logRecordAttributeCountLimit())
        assertEquals(256, behavior.logRecordAttributeValueLengthLimit())
    }

    @Test
    fun `should fall back to the dsl when resolving fails`() {
        val handler = FakeSdkErrorHandler()
        val dsl = logAttributeCountLimit(8)
        val behavior = read(
            env = mapOf(LOGRECORD_COUNT to "64"),
            dsl = dsl,
            resolver = object : BehaviorResolver {
                override fun resolve(
                    envars: OpenTelemetryBehavior?,
                    declarativeFile: OpenTelemetryBehavior?,
                    dsl: OpenTelemetryBehavior?,
                ): OpenTelemetryBehavior = error("resolver failed")
            },
            handler = handler,
        )
        assertEquals(dsl, behavior)
        assertEquals(1, handler.sdkCodeErrors.size)
    }

    private fun read(
        env: Map<String, String> = emptyMap(),
        dsl: OpenTelemetryBehavior? = null,
        dslProvider: () -> OpenTelemetryBehavior? = { dsl },
        fileContents: OpenTelemetryBehavior = OpenTelemetryBehavior(),
        configFilePath: String? = null,
        onRead: (String) -> Unit = {},
        resolver: BehaviorResolver = BehaviorResolverImpl(),
        handler: SdkErrorHandler = NoopSdkErrorHandler,
    ): OpenTelemetryBehavior {
        val reader = OpenTelemetryConfigReader(
            envVarReader = EnvVarReader(env::get),
            declarativeConfigReader = { path ->
                onRead(path)
                fileContents
            },
            behaviorResolver = resolver,
            sdkErrorHandler = handler,
        )
        return reader.read(dsl = dslProvider, configFilePath = configFilePath)
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
