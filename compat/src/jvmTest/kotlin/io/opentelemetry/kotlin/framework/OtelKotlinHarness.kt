package io.opentelemetry.kotlin.framework

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.createCompatOpenTelemetryImpl
import io.opentelemetry.kotlin.factory.CompatSdkFactory
import io.opentelemetry.kotlin.factory.CompatTracingIdFactory
import io.opentelemetry.kotlin.factory.TracingIdFactory
import io.opentelemetry.kotlin.toOtelJavaApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.random.Random

@OptIn(ExperimentalApi::class)
internal class OtelKotlinHarness(scheduler: TestCoroutineScheduler) :
    OtelKotlinTestRule(scheduler) {

    override val kotlinApi: OpenTelemetry by lazy {
        createCompatOpenTelemetryImpl(
            clock = fakeClock,
            config = {
                tracerProvider { tracerProviderConfig() }
                loggerProvider { loggerProviderConfig() }
            },
            sdkFactory = CompatSdkFactory(tracingIdFactory = FakeTracingIdFactory())
        )
    }

    val javaApi by lazy {
        kotlinApi.toOtelJavaApi()
    }
}

@OptIn(ExperimentalApi::class, ExperimentalStdlibApi::class)
private class FakeTracingIdFactory(
    private val impl: TracingIdFactory = CompatTracingIdFactory(),
) : TracingIdFactory by impl, OtelJavaIdGenerator {

    private val random: Random = Random(0)

    override fun generateSpanIdBytes(): ByteArray = generateTraceId().hexToByteArray()
    override fun generateTraceIdBytes(): ByteArray = generateSpanId().hexToByteArray()
    override fun generateSpanId(): String = randomHex(16)
    override fun generateTraceId(): String = randomHex(32)

    private fun randomHex(count: Int): String {
        val bytes = random.nextBytes(count / 2)
        return buildString(count / 2) {
            for (byte in bytes) {
                val unsigned = byte.toInt() and 0xFF
                append(unsigned.toString(16).padStart(2, '0'))
            }
        }
    }
}
