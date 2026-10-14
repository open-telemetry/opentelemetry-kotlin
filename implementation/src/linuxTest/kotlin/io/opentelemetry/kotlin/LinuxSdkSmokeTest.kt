@file:OptIn(ObsoleteWorkersApi::class)

package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.context.ImplicitContextStorageMode
import io.opentelemetry.kotlin.context.createContextKey
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.export.InMemoryLogRecordExporter
import io.opentelemetry.kotlin.logging.export.batchLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.inMemoryLogRecordExporter
import io.opentelemetry.kotlin.resource.deviceResourceDetector
import io.opentelemetry.kotlin.resource.hostResourceDetector
import io.opentelemetry.kotlin.tracing.export.InMemorySpanExporter
import io.opentelemetry.kotlin.tracing.export.batchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.inMemorySpanExporter
import kotlinx.coroutines.runBlocking
import kotlin.native.concurrent.ObsoleteWorkersApi
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.test.*

@OptIn(ExperimentalApi::class)
internal class LinuxSdkSmokeTest {
    @Test
    fun exportsTelemetryWithContextAndDetectedResources() = runBlocking {
        lateinit var spans: InMemorySpanExporter
        lateinit var logs: InMemoryLogRecordExporter
        val errors = FakeSdkErrorHandler()
        val api = createOpenTelemetry {
            serviceName = "linux-smoke-test"
            errorHandler(errors)
            context { storageMode = ImplicitContextStorageMode.THREAD_LOCAL }
            resourceDetection {
                detector(hostResourceDetector())
                detector(deviceResourceDetector())
            }
            tracerProvider {
                export {
                    spans = inMemorySpanExporter()
                    batchSpanProcessor(spans)
                }
            }
            loggerProvider {
                export {
                    logs = inMemoryLogRecordExporter()
                    batchLogRecordProcessor(logs)
                }
            }
        } as OpenTelemetrySdk
        val tracer = api.tracerProvider.getTracer("linux-test")
        val key = createContextKey<String>("thread-isolation")
        try {
            val parent = tracer.startSpan("parent")
            val scope = api.context.current().storeSpan(parent).set(key, "attached").attach()
            try {
                assertEquals("attached", api.context.current().get(key))
                val worker = Worker.start()
                try {
                    val otherThread = worker.execute(TransferMode.SAFE, { api.context to key }) { (contexts, key) ->
                        contexts.current().get(key)
                    }.result
                    assertNull(otherThread)
                } finally {
                    worker.requestTermination().result
                }
                tracer.startSpan("child").end()
                api.loggerProvider.getLogger("linux-test").emit("linux log")
            } finally {
                scope.detach()
                parent.end()
            }
            assertNull(api.context.current().get(key))
            assertEquals(OperationResultCode.Success, api.forceFlush())
            val exported = spans.exportedSpans
            assertEquals(setOf("parent", "child"), exported.map { it.name }.toSet())
            assertEquals(parent.spanContext, exported.single { it.name == "child" }.parent)
            val log = logs.exportedLogRecords.single()
            assertEquals("linux log", log.body)
            assertEquals(parent.spanContext, log.spanContext)
            for (resource in exported.map { it.resource } + log.resource) {
                assertEquals("linux-smoke-test", resource.attributes["service.name"])
                assertEquals("linux", resource.attributes["os.type"])
                assertEquals("Linux", resource.attributes["os.name"])
                assertTrue((resource.attributes["os.version"] as String).isNotBlank())
                assertFalse(resource.attributes.containsKey("device.manufacturer"))
            }
        } finally {
            assertEquals(OperationResultCode.Success, api.shutdown())
        }
        tracer.startSpan("after-shutdown").end()
        assertEquals(2, spans.exportedSpans.size)
        assertFalse(errors.hasErrors())
    }
}
