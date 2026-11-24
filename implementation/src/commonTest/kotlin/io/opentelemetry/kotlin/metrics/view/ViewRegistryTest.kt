package io.opentelemetry.kotlin.metrics.view

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.metrics.instrument.InstrumentDescriptor
import io.opentelemetry.kotlin.metrics.instrument.InstrumentKind
import io.opentelemetry.kotlin.metrics.instrument.InstrumentValueType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ViewRegistryTest {

    private val scope = FakeInstrumentationScopeInfo(name = "meter", version = "1.0", schemaUrl = "https://schema")
    private val instrument = InstrumentDescriptor(
        name = "http.server.duration",
        unit = "ms",
        description = null,
        kind = InstrumentKind.HISTOGRAM,
        valueType = InstrumentValueType.DOUBLE,
    )
    private val all = View(InstrumentSelector(name = "*"))

    private fun selects(
        selector: InstrumentSelector,
        instrument: InstrumentDescriptor = this.instrument,
        scope: FakeInstrumentationScopeInfo = this.scope,
    ): Boolean = ViewRegistry(listOf(View(selector))).findViews(instrument, scope).isNotEmpty()

    @Test
    fun `returns every matching view in registration order`() {
        val renamed = View(InstrumentSelector(name = "http.server.duration"), name = "renamed")
        val other = View(InstrumentSelector(meterName = "other"))
        val registry = ViewRegistry(listOf(renamed, other, all))

        assertEquals(listOf(renamed, all), registry.findViews(instrument, scope))
        assertEquals(listOf(renamed, other, all), registry.findViews(instrument, FakeInstrumentationScopeInfo(name = "other")))
    }

    @Test
    fun `returns no views when nothing matches`() {
        val registry = ViewRegistry(listOf(View(InstrumentSelector(name = "other"))))

        assertTrue(registry.findViews(instrument, scope).isEmpty())
    }

    @Test
    fun `registration is fixed at construction`() {
        val views = mutableListOf(all)
        val registry = ViewRegistry(views)
        views.clear()

        assertEquals(listOf(all), registry.findViews(instrument, scope))
    }

    @Test
    fun `selector without criteria matches every instrument`() {
        assertTrue(selects(InstrumentSelector()))
        assertTrue(selects(InstrumentSelector(name = "*")))
    }

    @Test
    fun `name supports wildcards and ignores case`() {
        listOf(
            "http.server.duration",
            "HTTP.Server.Duration",
            "http.*",
            "*.duration",
            "*server*",
            "h*r*n",
            "http.server.duratio?",
            "????.server.duration",
            "http***duration",
            "***",
            "*?",
            "http.server.duration*",
        ).forEach { pattern ->
            assertTrue(selects(InstrumentSelector(name = pattern)), pattern)
        }
        listOf(
            "",
            "http",
            "http.server",
            "*.size",
            "http.server.duration?",
            "?http.server.duration",
            "http***size",
            "*duration?",
            "http.+",
        ).forEach { pattern ->
            assertFalse(selects(InstrumentSelector(name = pattern)), pattern)
        }
    }

    @Test
    fun `other criteria must equal the instrument or meter value exactly`() {
        val everything = InstrumentSelector(
            kind = InstrumentKind.HISTOGRAM,
            unit = "ms",
            meterName = "meter",
            meterVersion = "1.0",
            meterSchemaUrl = "https://schema",
        )
        assertTrue(selects(everything))

        listOf(
            InstrumentSelector(unit = "MS"),
            InstrumentSelector(unit = "s"),
            InstrumentSelector(meterName = "other"),
            InstrumentSelector(meterVersion = "2.0"),
            InstrumentSelector(meterSchemaUrl = "https://other"),
        ).forEach { selector ->
            assertFalse(selects(selector), "$selector")
        }
    }

    @Test
    fun `criteria are additive`() {
        assertFalse(selects(InstrumentSelector(name = "http.*", meterName = "other")))
    }

    @Test
    fun `empty unit matches instruments without a unit`() {
        fun histogram(unit: String?) = InstrumentDescriptor(
            name = "bare",
            unit = unit,
            description = null,
            kind = InstrumentKind.HISTOGRAM,
            valueType = InstrumentValueType.DOUBLE,
        )

        assertTrue(selects(InstrumentSelector(unit = ""), histogram(unit = null)))
        assertTrue(selects(InstrumentSelector(unit = ""), histogram(unit = "")))
        assertFalse(selects(InstrumentSelector(unit = ""), histogram(unit = "ms")))
    }

    @Test
    fun `set criteria do not match absent values`() {
        val bare = InstrumentDescriptor(
            name = "bare",
            unit = null,
            description = null,
            kind = InstrumentKind.HISTOGRAM,
            valueType = InstrumentValueType.DOUBLE,
        )
        val bareScope = FakeInstrumentationScopeInfo(version = null, schemaUrl = null)

        assertFalse(selects(InstrumentSelector(unit = "ms"), bare, bareScope))
        assertFalse(selects(InstrumentSelector(meterVersion = "1.0"), bare, bareScope))
        assertFalse(selects(InstrumentSelector(meterSchemaUrl = "https://schema"), bare, bareScope))
    }
}
