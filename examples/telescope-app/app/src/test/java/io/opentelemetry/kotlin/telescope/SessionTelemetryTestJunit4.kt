@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.telescope

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.telescope.telemetry.SessionTelemetry
import io.opentelemetry.kotlin.testing.junit4.OpenTelemetryRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionTelemetryTestJunit4 {

    @get:Rule
    val openTelemetryRule: OpenTelemetryRule = OpenTelemetryRule()

    private lateinit var sessionTelemetry: SessionTelemetry

    @Before
    fun setUp() {
        val kotlinTracer = openTelemetryRule.getTracer("test")
        sessionTelemetry = SessionTelemetry(kotlinTracer)
    }

    @Test
    fun `a session span is created correctly`() {
        sessionTelemetry.onAppStart()
        sessionTelemetry.onAppStop()
        assertEquals("AppSession", openTelemetryRule.spans.single().name)
    }

    @Test
    fun `navigation creates child spans under session span`() {
        sessionTelemetry.onAppStart()
        sessionTelemetry.onNavigation("Screen1")
        sessionTelemetry.onNavigation("Screen2")
        sessionTelemetry.onAppStop()

        val spans = openTelemetryRule.spans
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

        val spans = openTelemetryRule.spans
        assertEquals(2, spans.size)
        assertTrue(spans.any { it.name == "AppSession" })
        assertTrue(spans.any { it.name == "Navigation to Screen1" })
    }

    @Test
    fun `stopping app without starting creates no spans`() {
        sessionTelemetry.onAppStop()
        assertEquals(0, openTelemetryRule.spans.size)
    }

    @Test
    fun `navigating without starting app creates no spans`() {
        sessionTelemetry.onNavigation("Screen1")
        assertEquals(0, openTelemetryRule.spans.size)
    }
}