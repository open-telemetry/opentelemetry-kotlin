package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.FakeSdkFactory
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.model.SpanContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class SpanLinkTest {

    private val linkLimit = 3
    private val fakeSpanContext = FakeSpanContext.INVALID
    private val otherFakeSpanContext = FakeSpanContext.VALID
    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())

    private lateinit var tracer: TracerImpl
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeSpanProcessor
    private lateinit var spanLimitConfig: SpanLimitConfig

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeSpanProcessor()
        spanLimitConfig = SpanLimitConfig(
            attributeCountLimit = fakeSpanLimitsConfig.attributeCountLimit,
            linkCountLimit = linkLimit,
            eventCountLimit = fakeSpanLimitsConfig.eventCountLimit,
            attributeCountPerEventLimit = fakeSpanLimitsConfig.attributeCountPerEventLimit,
            attributeCountPerLinkLimit = fakeSpanLimitsConfig.attributeCountPerLinkLimit
        )
        tracer = TracerImpl(
            clock,
            processor,
            FakeSdkFactory(),
            key,
            FakeResource(),
            spanLimitConfig
        )
    }

    @Test
    fun testSpanLink() {
        tracer.startSpan("test").apply {
            addLink(fakeSpanContext)
            addLink(otherFakeSpanContext) {
                setStringAttribute("foo", "bar")
            }
            end()
        }

        val links = retrieveLinks(2)
        assertLinkData(links[0], fakeSpanContext, emptyMap())
        assertLinkData(links[1], otherFakeSpanContext, mapOf("foo" to "bar"))
    }

    @Test
    fun testTwoSpanLinksWithSameKey() {
        tracer.startSpan("test").apply {
            addLink(fakeSpanContext)
            addLink(fakeSpanContext)
            end()
        }
        val links = retrieveLinks(1)
        assertLinkData(links[0], fakeSpanContext, emptyMap())
    }

    @Test
    fun testSpanLinkAfterEnd() {
        tracer.startSpan("test").apply {
            end()
            addLink(fakeSpanContext)
        }
        retrieveLinks(0)
    }

    @Test
    fun testSpanLinkDuringCreation() {
        tracer.startSpan("test", action = {
            addLink(fakeSpanContext)
            addLink(otherFakeSpanContext) {
                setStringAttribute("foo", "bar")
            }
        }).apply {
            end()
        }

        val links = retrieveLinks(2)
        assertLinkData(links[0], fakeSpanContext, emptyMap())
        assertLinkData(links[1], otherFakeSpanContext, mapOf("foo" to "bar"))
    }

    @Test
    fun testLinksLimitNotExceeded() {
        tracer.startSpan("test", action = {
            repeat(linkLimit + 1) {
                addLink(
                    FakeSpanContext(
                        "$it".repeat(32).hexToByteArray(),
                        "$it".repeat(16).hexToByteArray()
                    )
                )
            }
        }).apply {
            end()
        }

        retrieveLinks(3)
    }

    @Test
    fun testLinksLimitNotExceeded2() {
        tracer.startSpan("test").apply {
            repeat(linkLimit + 1) {
                addLink(
                    FakeSpanContext(
                        "$it".repeat(32).hexToByteArray(),
                        "$it".repeat(16).hexToByteArray()
                    )
                )
            }
            end()
        }

        retrieveLinks(3)
    }

    @Test
    fun testSpanLinkAttributesLimit() {
        val span = tracer.startSpan("test", action = {
            addLink(FakeSpanContext()) {
                repeat(fakeSpanLimitsConfig.attributeCountLimit + 1) {
                    setStringAttribute("foo$it", "bar")
                }
            }
        })
        val link = span.links.single()
        assertEquals(fakeSpanLimitsConfig.attributeCountLimit, link.attributes.size)
    }

    @Test
    fun testSpanLinkAttributesLimit2() {
        val span = tracer.startSpan("test").apply {
            addLink(FakeSpanContext(), attributes = {
                repeat(fakeSpanLimitsConfig.attributeCountLimit + 1) {
                    setStringAttribute("foo$it", "bar")
                }
            })
        }
        val link = span.links.single()
        assertEquals(fakeSpanLimitsConfig.attributeCountLimit, link.attributes.size)
    }

    private fun retrieveLinks(expected: Int): List<LinkData> {
        val links = processor.endCalls.single().links
        assertEquals(expected, links.size)
        return links
    }

    private fun assertLinkData(
        link: LinkData,
        spanContext: SpanContext,
        attrs: Map<String, Any>
    ) {
        assertEquals(spanContext, link.spanContext)
        assertEquals(attrs, link.attributes)
    }
}
