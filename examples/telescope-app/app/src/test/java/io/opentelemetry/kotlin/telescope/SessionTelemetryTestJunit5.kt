@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.telescope

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.telescope.telemetry.SessionTelemetry
import io.opentelemetry.kotlin.testing.junit5.OpenTelemetryExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SessionTelemetryTestJunit5 {

    @RegisterExtension
    val openTelemetryExtension: OpenTelemetryExtension = OpenTelemetryExtension()

    private lateinit var sessionTelemetry: SessionTelemetry

    @BeforeEach
    fun setUp() {
        val kotlinTracer = openTelemetryExtension.getTracer("test")
        sessionTelemetry = SessionTelemetry(kotlinTracer)
    }

    @Test
    fun `a session span is created correctly`() {
        sessionTelemetry.onAppStart()
        sessionTelemetry.onAppStop()
        assertEquals("AppSession", openTelemetryExtension.spans.single().name)
    }

    @Test
    fun `navigation creates child spans under session span`() {
        sessionTelemetry.onAppStart()
        sessionTelemetry.onNavigation("Screen1")
        sessionTelemetry.onNavigation("Screen2")
        sessionTelemetry.onAppStop()

        val spans = openTelemetryExtension.spans
        assertEquals(3, spans.size)

        val parentSpan = spans.find { it.name == "AppSession" } ?: throw AssertionError("Parent span not found")
        val childSpan1 = spans.find { it.name == "Navigation to Screen1" } ?: throw AssertionError("Screen1 span not found")
        val childSpan2 = spans.find { it.name == "Navigation to Screen2" } ?: throw AssertionError("Screen2 span not found")

        assertEquals(parentSpan.spanId, childSpan1.parentSpanId)
        assertEquals(parentSpan.spanId, childSpan2.parentSpanId)
    }

    @Test
    fun `navigating to same destination does not create new span`() {
        sessionTelemetry.onAppStart()
        sessionTelemetry.onNavigation("Screen1")
        sessionTelemetry.onNavigation("Screen1") // Same destination
        sessionTelemetry.onAppStop()

        val spans = openTelemetryExtension.spans
        assertEquals(2, spans.size)
        assertTrue(spans.any { it.name == "AppSession" })
        assertTrue(spans.any { it.name == "Navigation to Screen1" })
    }

    @Test
    fun `stopping app without starting creates no spans`() {
        sessionTelemetry.onAppStop()
        assertEquals(0, openTelemetryExtension.spans.size)
    }

    @Test
    fun `navigating without starting app creates no spans`() {
        sessionTelemetry.onNavigation("Screen1")
        assertEquals(0, openTelemetryExtension.spans.size)
    }
}