package io.opentelemetry.kotlin.framework

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.createCompatOpenTelemetryImpl
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.toOtelJavaApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.random.Random

internal class OtelKotlinHarness(scheduler: TestCoroutineScheduler) :
    OtelKotlinTestRule(scheduler) {

    override val kotlinApi: OpenTelemetry by lazy {
        createCompatOpenTelemetryImpl(
            clock = fakeClock,
            config = {
                tracerProvider { tracerProviderConfig() }
                loggerProvider { loggerProviderConfig() }
            },
            idGenerator = FakeIdGenerator()
        )
    }

    val javaApi by lazy {
        kotlinApi.toOtelJavaApi()
    }
}

private class FakeIdGenerator(
    private val impl: IdGenerator = CompatIdGenerator(),
) : IdGenerator by impl, OtelJavaIdGenerator {

    private val random: Random = Random(0)

    @OptIn(ExperimentalStdlibApi::class)
    override fun generateSpanIdBytes(): ByteArray = generateTraceId().hexToByteArray()

    @OptIn(ExperimentalStdlibApi::class)
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
