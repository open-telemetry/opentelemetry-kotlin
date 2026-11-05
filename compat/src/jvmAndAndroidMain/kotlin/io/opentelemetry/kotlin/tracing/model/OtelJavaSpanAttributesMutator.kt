package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaValue
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.toOtelJavaValue
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard
import io.opentelemetry.kotlin.error.userCode

/**
 * Writes attributes directly to an [OtelJavaSpan], which applies limits and truncation. Lists are copied
 * because the Java SDK stores them by reference.
 *
 * The span may be supplied by the host application, so any failure is reported to [sdkErrorHandler]
 * rather than escaping.
 */
internal class OtelJavaSpanAttributesMutator(
    private val span: OtelJavaSpan,
    private val sdkErrorHandler: SdkErrorHandler,
) : AttributesMutator {

    override fun setBooleanAttribute(key: String, value: Boolean) {
        sdkErrorHandler.sdkGuard("Span.setBooleanAttribute failed") {
            span.setAttribute(key, value)
        }
    }

    override fun setStringAttribute(key: String, value: String) {
        sdkErrorHandler.sdkGuard("Span.setStringAttribute failed") {
            span.setAttribute(key, value)
        }
    }

    override fun setLongAttribute(key: String, value: Long) {
        sdkErrorHandler.sdkGuard("Span.setLongAttribute failed") {
            span.setAttribute(key, value)
        }
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        sdkErrorHandler.sdkGuard("Span.setDoubleAttribute failed") {
            span.setAttribute(key, value)
        }
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
        sdkErrorHandler.sdkGuard("Span.setBooleanListAttribute failed") {
            span.setAttribute(OtelJavaAttributeKey.booleanArrayKey(key), userCode { value.toList() })
        }
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
        sdkErrorHandler.sdkGuard("Span.setStringListAttribute failed") {
            span.setAttribute(OtelJavaAttributeKey.stringArrayKey(key), userCode { value.toList() })
        }
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
        sdkErrorHandler.sdkGuard("Span.setLongListAttribute failed") {
            span.setAttribute(OtelJavaAttributeKey.longArrayKey(key), userCode { value.toList() })
        }
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
        sdkErrorHandler.sdkGuard("Span.setDoubleListAttribute failed") {
            span.setAttribute(OtelJavaAttributeKey.doubleArrayKey(key), userCode { value.toList() })
        }
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        sdkErrorHandler.sdkGuard("Span.setByteArrayAttribute failed") {
            span.setAttribute(OtelJavaAttributeKey.valueKey(key), OtelJavaValue.of(value))
        }
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        sdkErrorHandler.sdkGuard("Span.setAnyValueAttribute failed") {
            span.setAttribute(OtelJavaAttributeKey.valueKey(key), userCode { value.toOtelJavaValue() })
        }
    }
}
