@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.testing.junit5

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetrySdk
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.testing.common.InMemorySpanExporter
import io.opentelemetry.kotlin.testing.common.InMemorySpanProcessor
import io.opentelemetry.kotlin.toOtelKotlinApi
import io.opentelemetry.kotlin.tracing.Tracer
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * A JUnit5 extension which sets up an [OpenTelemetry] instance for testing, resetting state before each test.
 *
 * ```kotlin
 * @ExtendWith(OpenTelemetryExtension::class)
 * class CoolTest {
 *
 *   private lateinit var tracer: Tracer
 *
 *   @BeforeEach
 *   fun setUp(extension: OpenTelemetryExtension) {
 *     tracer = extension.getTracer("test")
 *   }
 *
 *   @Test
 *   fun test(extension: OpenTelemetryExtension) {
 *     tracer.createSpan("hello").end()
 *     assertTrue(
 *          extension.spans.any  { it.name == "hello" }
 *     )
 *   }
 * }
 * ```
 */
class OpenTelemetryExtension : BeforeEachCallback {

    private val spanExporter = InMemorySpanExporter()

    private val tracerProvider: OtelJavaSdkTracerProvider = OtelJavaSdkTracerProvider.builder()
        .addSpanProcessor(InMemorySpanProcessor(spanExporter))
        .build()

    private val sdk = OtelJavaOpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .build()

    val openTelemetry: OpenTelemetry = sdk.toOtelKotlinApi()

    val spans: List<OtelJavaSpanData>
        get() = spanExporter.exportedSpans

    fun getTracer(name: String): Tracer {
        return openTelemetry.tracerProvider.getTracer(name)
    }

    override fun beforeEach(context: ExtensionContext?) {
        spanExporter.reset()
    }
}
