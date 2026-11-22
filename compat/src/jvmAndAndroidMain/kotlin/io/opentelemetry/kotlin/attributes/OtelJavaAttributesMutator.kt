package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributesBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteLogRecord
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaValue
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.reportSdkCodeError
import io.opentelemetry.kotlin.error.userCode

/**
 * Destination for a typed opentelemetry-java attribute write.
 */
internal interface OtelJavaAttributeSink {
    fun <T : Any> put(key: OtelJavaAttributeKey<T>, value: T)
}

internal fun OtelJavaSpan.attributeSink(): OtelJavaAttributeSink = object : OtelJavaAttributeSink {
    override fun <T : Any> put(key: OtelJavaAttributeKey<T>, value: T) {
        setAttribute(key, value)
    }
}

internal fun OtelJavaAttributesBuilder.attributeSink(): OtelJavaAttributeSink = object : OtelJavaAttributeSink {
    override fun <T : Any> put(key: OtelJavaAttributeKey<T>, value: T) {
        this@attributeSink.put(key, value)
    }
}

internal fun OtelJavaReadWriteLogRecord.attributeSink(): OtelJavaAttributeSink = object : OtelJavaAttributeSink {
    override fun <T : Any> put(key: OtelJavaAttributeKey<T>, value: T) {
        setAttribute(key, value)
    }
}

/**
 * The single mapping from Kotlin attribute setters to opentelemetry-java attribute keys. Bytes and [AnyValue] are
 * stored as `VALUE` because opentelemetry-java has no bytes attribute type. Lists are copied because the Java SDK
 * stores them by reference.
 *
 * When [sdkErrorHandler] is supplied, any failure is reported to it as "[errorPrefix].<setter> failed" rather than
 * escaping, with failures in caller-supplied values reported as user code errors.
 */
internal class OtelJavaAttributesMutator(
    private val sink: OtelJavaAttributeSink,
    private val sdkErrorHandler: SdkErrorHandler? = null,
    private val errorPrefix: String = "Attributes",
) : AttributesMutator {

    override fun setBooleanAttribute(key: String, value: Boolean) = guard("setBooleanAttribute") {
        sink.put(OtelJavaAttributeKey.booleanKey(key), value)
    }

    override fun setStringAttribute(key: String, value: String) = guard("setStringAttribute") {
        sink.put(OtelJavaAttributeKey.stringKey(key), value)
    }

    override fun setLongAttribute(key: String, value: Long) = guard("setLongAttribute") {
        sink.put(OtelJavaAttributeKey.longKey(key), value)
    }

    override fun setDoubleAttribute(key: String, value: Double) = guard("setDoubleAttribute") {
        sink.put(OtelJavaAttributeKey.doubleKey(key), value)
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) = guard("setBooleanListAttribute") {
        sink.put(OtelJavaAttributeKey.booleanArrayKey(key), caller { value.toList() })
    }

    override fun setStringListAttribute(key: String, value: List<String>) = guard("setStringListAttribute") {
        sink.put(OtelJavaAttributeKey.stringArrayKey(key), caller { value.toList() })
    }

    override fun setLongListAttribute(key: String, value: List<Long>) = guard("setLongListAttribute") {
        sink.put(OtelJavaAttributeKey.longArrayKey(key), caller { value.toList() })
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) = guard("setDoubleListAttribute") {
        sink.put(OtelJavaAttributeKey.doubleArrayKey(key), caller { value.toList() })
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) = guard("setByteArrayAttribute") {
        sink.put(OtelJavaAttributeKey.valueKey(key), OtelJavaValue.of(value))
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) = guard("setAnyValueAttribute") {
        sink.put(OtelJavaAttributeKey.valueKey(key), caller { value.toOtelJavaValue() })
    }

    /**
     * Reports failures to [sdkErrorHandler] when one is supplied. The message is only built on failure because this
     * is called on every attribute write.
     */
    @Suppress("TooGenericExceptionCaught")
    private inline fun guard(setter: String, action: () -> Unit) {
        val handler = sdkErrorHandler ?: return action()
        try {
            action()
        } catch (exc: Throwable) {
            handler.reportSdkCodeError(exc, "$errorPrefix.$setter failed")
        }
    }

    /**
     * Tags failures in caller-supplied values as user code. Only applied when guarded, since [userCode] relies on an
     * enclosing guard to unwrap its exception.
     */
    private inline fun <T> caller(action: () -> T): T = when (sdkErrorHandler) {
        null -> action()
        else -> userCode(action)
    }
}
