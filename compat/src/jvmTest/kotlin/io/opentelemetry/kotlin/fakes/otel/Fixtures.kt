import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaEventData
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaStatusData
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanData

internal val fakeOtelJavaContext = OtelJavaSpanContext.create(
    "19bb482ec1c7e6b2f10fb89e0ccc85fa",
    "342eb9c7f8cb54ff",
    OtelJavaTraceFlags.getDefault(),
    OtelJavaTraceState.getDefault()
)
internal val fakeOtelJavaAttributes = OtelJavaAttributes.of(OtelJavaAttributeKey.stringKey("key"), "value")
internal val fakeOtelJavaEventData = OtelJavaEventData.create(150, "event", fakeOtelJavaAttributes)
internal val fakeOtelJavaLinkData = OtelJavaLinkData.create(OtelJavaSpanContext.getInvalid(), fakeOtelJavaAttributes)
internal val fakeOtelJavaResource = OtelJavaResource.create(fakeOtelJavaAttributes, "http://example.com/foo")
internal val fakeInProgressOtelJavaSpanData = FakeOtelJavaSpanData(
    implName = "fake_span",
    implSpanContext = fakeOtelJavaContext,
    implAttributes = fakeOtelJavaAttributes,
    implEventData = listOf(fakeOtelJavaEventData),
    implLinkData = listOf(fakeOtelJavaLinkData),
    implStartNs = 1681972471806000000L,
    implEndNs = 0,
    implEnded = false,
    implStatusData = OtelJavaStatusData.unset(),
    implResource = fakeOtelJavaResource
)
