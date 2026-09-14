package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.DefaultImplicitContextStorage
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.context.FakeImplicitContextStorage
import io.opentelemetry.kotlin.context.ImplicitContextStorageMode
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.propagation.CompositeTextMapPropagator
import io.opentelemetry.kotlin.propagation.W3CBaggagePropagator
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class OpenTelemetryConfigImplTest {

    private val clock = FakeClock()

    @Test
    fun testDefaultConfig() {
        val cfg = OpenTelemetryConfigImpl(clock)
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        assertNull(resolver.generateTracingConfig().processor)
        assertNull(resolver.generateLoggingConfig().processor)
        assertNull(cfg.contextConfig.storageMode)
        assertSame(NoopOpenTelemetry.propagator, cfg.propagatorCfg.buildPropagator())
    }

    @Test
    fun testPropagatorOverride() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            propagator { w3cBaggage() }
        }
        assertSame(W3CBaggagePropagator, cfg.propagatorCfg.buildPropagator())
    }

    @Test
    fun testCompositePropagatorOverride() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            propagator { composite(w3cBaggage()) }
        }
        val composite = cfg.propagatorCfg.buildPropagator()
        assertIs<CompositeTextMapPropagator>(composite)
        assertEquals(listOf("baggage"), composite.fields().toList())
    }

    @Test
    fun testOverrideConfig() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.loggerProvider {
            export { FakeLogRecordProcessor() }
        }
        cfg.tracerProvider {
            export { FakeSpanProcessor() }
        }
        cfg.context {
            assertNull(storageMode)
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        assertNotNull(resolver.generateTracingConfig().processor)
        assertNotNull(resolver.generateLoggingConfig().processor)
    }

    @Test
    fun testIdGeneratorDefault() {
        val cfg = OpenTelemetryConfigImpl(clock)
        assertNotNull(SdkConfigFactory(cfg, OpenTelemetryBehavior()).idGenerator)
    }

    @Test
    fun testIdGeneratorOverride() {
        val custom = FakeIdGenerator()
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            idGenerator { custom }
        }
        assertSame(custom, SdkConfigFactory(cfg, OpenTelemetryBehavior()).idGenerator)
    }

    @Test
    fun testGlobalAttrLimits() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            attributeLimits {
                attributeCountLimit = 64
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        assertEquals(64, resolver.generateTracingConfig().spanLimits.attributeCountLimit)
        assertEquals(64, resolver.generateLoggingConfig().logLimits.attributeCountLimit)
    }

    @Test
    fun testLocalAttrLimits() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            attributeLimits {
                attributeCountLimit = 64
            }
            tracerProvider {
                spanLimits {
                    attributeCountLimit = 32
                }
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        assertEquals(32, resolver.generateTracingConfig().spanLimits.attributeCountLimit)
        assertEquals(64, resolver.generateLoggingConfig().logLimits.attributeCountLimit)
    }

    @Test
    fun testLocalSpanLimits() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            tracerProvider {
                spanLimits {
                    linkCountLimit = 8
                    eventCountLimit = 16
                    attributeCountPerEventLimit = 32
                    attributeCountPerLinkLimit = 64
                }
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        val spanLimits = resolver.generateTracingConfig().spanLimits
        assertEquals(8, spanLimits.linkCountLimit)
        assertEquals(16, spanLimits.eventCountLimit)
        assertEquals(32, spanLimits.attributeCountPerEventLimit)
        assertEquals(64, spanLimits.attributeCountPerLinkLimit)
    }

    @Test
    fun testLocalAttrLimits2() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            attributeLimits {
                attributeCountLimit = 64
            }
            tracerProvider {
                spanLimits {
                    attributeValueLengthLimit = 256
                }
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        with(resolver.generateTracingConfig().spanLimits) {
            assertEquals(64, attributeCountLimit)
            assertEquals(256, attributeValueLengthLimit)
        }
        assertEquals(64, resolver.generateLoggingConfig().logLimits.attributeCountLimit)
    }

    @Test
    fun testSignalZeroAttrLimitBeatsGlobal() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
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
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        assertEquals(0, resolver.generateTracingConfig().spanLimits.attributeCountLimit)
        assertEquals(0, resolver.generateLoggingConfig().logLimits.attributeCountLimit)
    }

    @Test
    fun testNegativeGlobalAttrLimitFallsBackToDefault() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            attributeLimits {
                attributeCountLimit = -1
                attributeValueLengthLimit = -1
            }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        with(resolver.generateTracingConfig().spanLimits) {
            assertEquals(DEFAULT_ATTRIBUTE_LIMIT, attributeCountLimit)
            assertEquals(DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT, attributeValueLengthLimit)
        }
        with(resolver.generateLoggingConfig().logLimits) {
            assertNull(attributeCountLimit)
            assertNull(attributeValueLengthLimit)
        }
    }

    @Test
    fun testDefaultStorage() {
        val cfg = OpenTelemetryConfigImpl(clock)
        val rootContext = FakeContext()
        val storage = cfg.contextConfig.generateStorage { rootContext }
        assertTrue(storage is DefaultImplicitContextStorage)
    }

    @Test
    fun testThreadLocalStorage() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            context {
                storageMode = ImplicitContextStorageMode.THREAD_LOCAL
            }
        }
        val storage = cfg.contextConfig.generateStorage(::FakeContext)
        val root = FakeContext()
        val rootStorage = cfg.contextConfig.generateStorage { root }
        assertNotNull(storage)
        assertSame(root, rootStorage.implicitContext())
    }

    @Test
    fun testCustomStorage() {
        val custom = FakeImplicitContextStorage()
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            context {
                storage { custom }
            }
        }
        assertSame(custom, cfg.contextConfig.generateStorage(::FakeContext))
    }

    @Test
    fun testCustomStorageOverridesStorageMode() {
        val custom = FakeImplicitContextStorage()
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            context {
                storageMode = ImplicitContextStorageMode.GLOBAL
                storage { custom }
            }
        }
        assertSame(custom, cfg.contextConfig.generateStorage(::FakeContext))
    }

    @Test
    fun testCustomStorageReceivesRootSupplier() {
        val root = FakeContext()
        var captured: (() -> Context)? = null
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            context {
                storage { rootSupplier ->
                    captured = rootSupplier
                    DefaultImplicitContextStorage(rootSupplier)
                }
            }
        }
        val storage = cfg.contextConfig.generateStorage { root }
        assertSame(root, captured?.invoke())
        assertSame(root, storage.implicitContext())
    }

    @Test
    fun testDefaultErrorHandlerDiscardsReports() {
        val cfg = OpenTelemetryConfigImpl(clock)
        // no handler configured, so reports are swallowed rather than thrown
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        resolver.generateTracingConfig().sdkErrorHandler.onError(sdkError())
    }

    @Test
    fun testCustomErrorHandlerReceivesReports() {
        val handler = FakeSdkErrorHandler()
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            errorHandler(handler)
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
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
        OpenTelemetryConfigImpl(clock).apply {
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
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            errorHandler(first)
            errorHandler(second)
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        resolver.generateTracingConfig().sdkErrorHandler.onError(sdkError())
        assertTrue(first.errors.isEmpty())
        assertEquals(1, second.errors.size)
    }

    @Test
    fun testErrorHandlerAcceptsLambda() {
        val received = mutableListOf<SdkError>()
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            errorHandler { received.add(it) }
        }
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        resolver.generateTracingConfig().sdkErrorHandler.onError(sdkError())
        assertEquals(1, received.size)
        assertIs<SdkError.ApiMisuse>(received.single())
    }

    @Test
    fun testDefaultAttrLimits() {
        val cfg = OpenTelemetryConfigImpl(clock)
        val behavior = defaultBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val resolver = SdkConfigFactory(cfg, behavior)
        with(resolver.generateTracingConfig().spanLimits) {
            assertEquals(DEFAULT_ATTRIBUTE_LIMIT, attributeCountLimit)
            assertEquals(DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT, attributeValueLengthLimit)
        }
        with(resolver.generateLoggingConfig().logLimits) {
            assertNull(attributeCountLimit)
            assertNull(attributeValueLengthLimit)
        }
    }

    private fun sdkError(message: String = "boom") =
        SdkError.ApiMisuse("TestApi", message, SdkErrorSeverity.WARNING)
}
