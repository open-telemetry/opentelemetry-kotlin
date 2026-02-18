package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalApi::class)
internal class OtelJavaLogRecordBuilderAdapter(private val impl: Logger) :
    OtelJavaLogRecordBuilder {

    private var timestamp: Long? = null
    private var observedTimestamp: Long? = null
    private var context: OtelJavaContext? = null
    private var severity: OtelJavaSeverity? = null
    private var severityText: String? = null
    private var body: String? = null
    private val attrs = ConcurrentHashMap<String, String>()

    override fun setTimestamp(timestamp: Long, unit: TimeUnit): OtelJavaLogRecordBuilder {
        this.timestamp = unit.toNanos(timestamp)
        return this
    }

    override fun setTimestamp(instant: Instant): OtelJavaLogRecordBuilder {
        runCatching {
            this.timestamp = instant.convertToNanos()
        }
        return this
    }

    override fun setObservedTimestamp(timestamp: Long, unit: TimeUnit): OtelJavaLogRecordBuilder {
        this.observedTimestamp = unit.toNanos(timestamp)
        return this
    }

    override fun setObservedTimestamp(instant: Instant): OtelJavaLogRecordBuilder {
        runCatching {
            this.observedTimestamp = instant.convertToNanos()
        }
        return this
    }

    override fun setContext(context: OtelJavaContext): OtelJavaLogRecordBuilder {
        this.context = context
        return this
    }

    override fun setSeverity(severity: OtelJavaSeverity): OtelJavaLogRecordBuilder {
        this.severity = severity
        return this
    }

    override fun setSeverityText(severityText: String): OtelJavaLogRecordBuilder {
        this.severityText = severityText
        return this
    }

    override fun setBody(body: String): OtelJavaLogRecordBuilder {
        this.body = body
        return this
    }

    override fun <T : Any?> setAttribute(
        key: OtelJavaAttributeKey<T>,
        value: T?
    ): OtelJavaLogRecordBuilder {
        attrs[key.key] = value.toString()
        return this
    }

    override fun emit() {
        impl.emit(
            body = body,
            timestamp = timestamp,
            observedTimestamp = observedTimestamp,
            context = context?.toOtelKotlinContext(),
            severityNumber = severity?.toOtelKotlinSeverityNumber(),
            severityText = severityText,
            attributes = { attrs.forEach { setStringAttribute(it.key, it.value) } }
        )
    }

    private fun Instant.convertToNanos(): Long? {
        runCatching {
            val seconds: Long = epochSecond
            val nanos: Int = nano
            return seconds * 1000000000L + nanos
        }
        return null
    }
}
