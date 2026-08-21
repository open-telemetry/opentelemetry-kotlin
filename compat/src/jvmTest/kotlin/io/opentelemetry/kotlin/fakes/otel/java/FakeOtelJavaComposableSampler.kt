package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaComposableSampler
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingIntent
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind

internal class FakeOtelJavaComposableSampler(
    var intent: OtelJavaSamplingIntent = FakeOtelJavaSamplingIntent(),
    private val fakeDescription: String = "FakeOtelJavaComposableSampler",
) : OtelJavaComposableSampler {

    var lastContext: OtelJavaContext? = null
        private set
    var lastTraceId: String? = null
        private set
    var lastName: String? = null
        private set
    var lastSpanKind: OtelJavaSpanKind? = null
        private set
    var lastAttributes: OtelJavaAttributes? = null
        private set
    var lastLinks: List<OtelJavaLinkData>? = null
        private set

    override fun getSamplingIntent(
        context: OtelJavaContext,
        traceId: String,
        name: String,
        spanKind: OtelJavaSpanKind,
        attributes: OtelJavaAttributes,
        links: List<OtelJavaLinkData>,
    ): OtelJavaSamplingIntent {
        lastContext = context
        lastTraceId = traceId
        lastName = name
        lastSpanKind = spanKind
        lastAttributes = attributes
        lastLinks = links
        return intent
    }

    override fun getDescription(): String = fakeDescription
}
