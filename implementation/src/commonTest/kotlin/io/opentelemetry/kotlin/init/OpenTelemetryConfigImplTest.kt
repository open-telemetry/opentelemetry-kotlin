package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT
import io.opentelemetry.kotlin.behavior.BehaviorResolverImpl
import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.propagation.FakeTextMapPropagator
import io.opentelemetry.kotlin.propagation.createW3CBaggagePropagator
import io.opentelemetry.kotlin.propagation.createW3CTraceContextPropagator
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class OpenTelemetryConfigImplTest {

    @Test
    fun testShutdownTimeoutValidation() {
        val errors = FakeSdkErrorHandler()
        val cfg = OpenTelemetryConfigImpl().apply { errorHandler(errors) }
        assertEquals(3000L, cfg.shutdownTimeoutMs)
        cfg.shutdownTimeout(6000)
        assertEquals(6000L, cfg.shutdownTimeoutMs)
        assertEquals(0, errors.apiMisuses.size)
        listOf(0L, -1L).forEach { timeout ->
            cfg.shutdownTimeout(timeout)
            assertEquals(6000L, cfg.shutdownTimeoutMs)
        }
        assertEquals(2, errors.apiMisuses.size)
    }

    @Test
    fun testDefaultConfig() {
        val cfg = OpenTelemetryConfigImpl()
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val resolver = SdkConfigFactory(cfg, behavior)
        assertNull(resolver.generateTracingConfig().processor)
        assertNull(resolver.generateLoggingConfig().processor)
        assertNull(cfg.contextConfig.storage)
        assertSame(NoopOpenTelemetry.propagator, cfg.propagator)
    }

    @Test
    fun testPropagatorOverride() {
        val cfg = OpenTelemetryConfigImpl().apply {
            propagator { w3cBaggage() }
        }
        assertSame(createW3CBaggagePropagator(), cfg.propagator)
    }

    @Test
    fun testCompositePropagatorOverride() {
        val cfg = OpenTelemetryConfigImpl().apply {
            propagator { composite(w3cBaggage()) }
        }
        val composite = cfg.propagator
        assertEquals(listOf("baggage"), composite.fields().toList())
    }

    @Test
    fun testCustomPropagatorOverride() {
        val custom = FakeTextMapPropagator()
        val cfg = OpenTelemetryConfigImpl().apply {
            propagator { custom }
        }
        assertSame(custom, cfg.propagator)
    }

    @Test
    fun testPropagatorUsesReturnValueNotLastBuilderCall() {
        val cfg = OpenTelemetryConfigImpl().apply {
            propagator {
                val traceContext = w3cTraceContext()
                w3cBaggage()
                traceContext
            }
        }
        assertSame(createW3CTraceContextPropagator(), cfg.propagator)
    }

    @Test
    fun testOverrideConfig() {
        val cfg = OpenTelemetryConfigImpl()
        cfg.loggerProvider {
            export { FakeLogRecordProcessor() }
        }
        cfg.tracerProvider {
            export { FakeSpanProcessor() }
        }
        cfg.context {
            assertNull(storage)
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val resolver = SdkConfigFactory(cfg, behavior)
        assertNotNull(resolver.generateTracingConfig().processor)
        assertNotNull(resolver.generateLoggingConfig().processor)
    }

    @Test
    fun testIdGeneratorDefault() {
        val cfg = OpenTelemetryConfigImpl()
        assertIs<IdGeneratorImpl>(SdkConfigFactory(cfg, OpenTelemetryBehavior()).idGenerator)
    }

    @Test
    fun testIdGeneratorFromResolvedBehavior() {
        val behavior = OpenTelemetryBehavior(
            tracerProvider = TracerProviderBehavior(idGenerator = IdGeneratorBehavior.Random),
        )

        assertIs<IdGeneratorImpl>(SdkConfigFactory(OpenTelemetryConfigImpl(), behavior).idGenerator)
    }

    @Test
    fun testDslIdGeneratorOverridesResolvedBehavior() {
        val custom = FakeIdGenerator()
        val cfg = OpenTelemetryConfigImpl().apply {
            idGenerator { custom }
        }
        val behavior = BehaviorResolverImpl().resolve(
            envars = null,
            declarativeFile = OpenTelemetryBehavior(
                tracerProvider = TracerProviderBehavior(idGenerator = IdGeneratorBehavior.Random),
            ),
            dsl = cfg.toBehavior(),
        )

        assertSame(custom, SdkConfigFactory(cfg, behavior).idGenerator)
    }

    @Test
    fun testGlobalAttrLimits() {
        val cfg = OpenTelemetryConfigImpl().apply {
            attributeLimits {
                attributeCountLimit = 64
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        assertEquals(64, behavior.spanLimitConfig().attributeCountLimit)
        assertEquals(64, behavior.logLimits().attributeCountLimit)
    }

    @Test
    fun testLocalAttrLimits() {
        val cfg = OpenTelemetryConfigImpl().apply {
            attributeLimits {
                attributeCountLimit = 64
            }
            tracerProvider {
                spanLimits {
                    attributeCountLimit = 32
                }
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        assertEquals(32, behavior.spanLimitConfig().attributeCountLimit)
        assertEquals(64, behavior.logLimits().attributeCountLimit)
    }

    @Test
    fun testLocalSpanLimits() {
        val cfg = OpenTelemetryConfigImpl().apply {
            tracerProvider {
                spanLimits {
                    linkCountLimit = 8
                    eventCountLimit = 16
                    attributeCountPerEventLimit = 32
                    attributeCountPerLinkLimit = 64
                }
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val spanLimits = behavior.spanLimitConfig()
        assertEquals(8, spanLimits.linkCountLimit)
        assertEquals(16, spanLimits.eventCountLimit)
        assertEquals(32, spanLimits.attributeCountPerEventLimit)
        assertEquals(64, spanLimits.attributeCountPerLinkLimit)
    }

    @Test
    fun testLocalAttrLimits2() {
        val cfg = OpenTelemetryConfigImpl().apply {
            attributeLimits {
                attributeCountLimit = 64
            }
            tracerProvider {
                spanLimits {
                    attributeValueLengthLimit = 256
                }
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        with(behavior.spanLimitConfig()) {
            assertEquals(64, attributeCountLimit)
            assertEquals(256, attributeValueLengthLimit)
        }
        assertEquals(64, behavior.logLimits().attributeCountLimit)
    }

    @Test
    fun testLocalLogLimits() {
        val cfg = OpenTelemetryConfigImpl().apply {
            attributeLimits {
                attributeCountLimit = 64
                attributeValueLengthLimit = 256
            }
            loggerProvider {
                logLimits {
                    attributeCountLimit = 8
                    attributeValueLengthLimit = 16
                }
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val logLimits = behavior.logLimits()
        assertEquals(8, logLimits.attributeCountLimit)
        assertEquals(16, logLimits.attributeValueLengthLimit)
    }

    @Test
    fun testLocalLogLimits2() {
        val cfg = OpenTelemetryConfigImpl().apply {
            attributeLimits {
                attributeCountLimit = 64
            }
            loggerProvider {
                logLimits {
                    attributeValueLengthLimit = 256
                }
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        with(behavior.logLimits()) {
            assertEquals(64, attributeCountLimit)
            assertEquals(256, attributeValueLengthLimit)
        }
    }

    @Test
    fun testSignalZeroAttrLimitBeatsGlobal() {
        val cfg = OpenTelemetryConfigImpl().apply {
            attributeLimits {
                attributeCountLimit = 64
            }
            tracerProvider {
                spanLimits {
                    attributeCountLimit = 0
                }
            }
            loggerProvider {
                logLimits {
                    attributeCountLimit = 0
                }
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        assertEquals(0, behavior.spanLimitConfig().attributeCountLimit)
        assertEquals(0, behavior.logLimits().attributeCountLimit)
    }

    @Test
    fun testNegativeGlobalAttrLimitFallsBackToDefault() {
        val cfg = OpenTelemetryConfigImpl().apply {
            attributeLimits {
                attributeCountLimit = -1
                attributeValueLengthLimit = -1
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        with(behavior.spanLimitConfig()) {
            assertEquals(DEFAULT_ATTRIBUTE_LIMIT, attributeCountLimit)
            assertEquals(DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT, attributeValueLengthLimit)
        }
        with(behavior.logLimits()) {
            assertNull(attributeCountLimit)
            assertNull(attributeValueLengthLimit)
        }
    }

    @Test
    fun testDefaultErrorHandlerDiscardsReports() {
        val cfg = OpenTelemetryConfigImpl()
        // no handler configured, so reports are swallowed rather than thrown
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val resolver = SdkConfigFactory(cfg, behavior)
        resolver.generateTracingConfig().sdkErrorHandler.onError(sdkError())
    }

    @Test
    fun testCustomErrorHandlerReceivesReports() {
        val handler = FakeSdkErrorHandler()
        val cfg = OpenTelemetryConfigImpl().apply {
            errorHandler(handler)
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val resolver = SdkConfigFactory(cfg, behavior)
        resolver.generateTracingConfig().sdkErrorHandler.onError(sdkError("trace"))
        resolver.generateLoggingConfig().sdkErrorHandler.onError(sdkError("log"))
        resolver.generateMetricsConfig().sdkErrorHandler.onError(sdkError("metric"))
        assertEquals(listOf("trace", "log", "metric"), handler.errors.map { it.message })
    }

    @Test
    fun testCustomErrorHandlerReceivesReportsWhenConfiguredLast() {
        val handler = FakeSdkErrorHandler()
        var captured: SdkErrorHandler? = null
        OpenTelemetryConfigImpl().apply {
            tracerProvider {
                export {
                    captured = sdkErrorHandler
                    FakeSpanProcessor()
                }
            }
            errorHandler(handler)
        }
        // the processor above was built before the handler was configured
        assertNotNull(captured).onError(sdkError())
        assertEquals(1, handler.apiMisuses.size)
    }

    @Test
    fun testLastConfiguredErrorHandlerWins() {
        val first = FakeSdkErrorHandler()
        val second = FakeSdkErrorHandler()
        val cfg = OpenTelemetryConfigImpl().apply {
            errorHandler(first)
            errorHandler(second)
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val resolver = SdkConfigFactory(cfg, behavior)
        resolver.generateTracingConfig().sdkErrorHandler.onError(sdkError())
        assertTrue(first.errors.isEmpty())
        assertEquals(1, second.errors.size)
    }

    @Test
    fun testErrorHandlerAcceptsLambda() {
        val received = mutableListOf<SdkError>()
        val cfg = OpenTelemetryConfigImpl().apply {
            errorHandler { received.add(it) }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val resolver = SdkConfigFactory(cfg, behavior)
        resolver.generateTracingConfig().sdkErrorHandler.onError(sdkError())
        assertEquals(1, received.size)
        assertIs<SdkError.ApiMisuse>(received.single())
    }

    @Test
    fun testDefaultAttrLimits() {
        val cfg = OpenTelemetryConfigImpl()
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        with(behavior.spanLimitConfig()) {
            assertEquals(DEFAULT_ATTRIBUTE_LIMIT, attributeCountLimit)
            assertEquals(DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT, attributeValueLengthLimit)
        }
        with(behavior.logLimits()) {
            assertNull(attributeCountLimit)
            assertNull(attributeValueLengthLimit)
        }
    }

    private fun sdkError(message: String = "boom") =
        SdkError.ApiMisuse("TestApi", message, SdkErrorSeverity.WARNING)

    private fun OpenTelemetryBehavior.spanLimitConfig() = SpanLimitConfig(tracerProvider?.spanLimits ?: SpanLimitsBehavior())

    private fun OpenTelemetryBehavior.logLimits() = loggerProvider?.logLimits ?: LogLimitsBehavior()
}
