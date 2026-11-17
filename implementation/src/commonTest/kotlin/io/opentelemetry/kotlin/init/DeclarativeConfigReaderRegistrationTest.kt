package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.config.envar.reader.EnvVarReader
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class DeclarativeConfigReaderRegistrationTest {

    @Test
    fun `OTEL_CONFIG_FILE without a registered reader reports one error and falls back to the environment`() {
        val handler = FakeSdkErrorHandler()
        val cfg = OpenTelemetryConfigImpl().apply { errorHandler(handler) }
        val env = mapOf(
            "OTEL_CONFIG_FILE" to "otel.yaml",
            "OTEL_ATTRIBUTE_COUNT_LIMIT" to "64",
        )
        val behavior = defaultBehaviorReader(
            envVarReader = EnvVarReader(env::get),
            sdkErrorHandler = cfg.sdkErrorHandler,
            declarativeConfigReader = cfg.declarativeConfigReader,
        ).read(cfg.configFilePath, cfg::toBehavior)

        assertEquals(64, behavior.attributeLimits?.attributeCountLimit)
        assertEquals(1, handler.errors.size)
        val misuse = handler.apiMisuses.single()
        assertEquals("OTEL_CONFIG_FILE", misuse.api)
        assertTrue(misuse.message.contains("otel.yaml"))
        assertEquals(SdkErrorSeverity.ERROR, misuse.severity)
    }

    @Test
    fun `an unsupported reader is reported and the declaration ignored`() {
        val handler = FakeSdkErrorHandler()
        val cfg = OpenTelemetryConfigImpl().apply {
            errorHandler(handler)
            configFile(object : DeclarativeConfigReader {}, "otel.yaml")
        }
        assertNull(cfg.declarativeConfigReader)
        assertNull(cfg.configFilePath)
        assertEquals("configFile", handler.apiMisuses.single().api)
    }
}
