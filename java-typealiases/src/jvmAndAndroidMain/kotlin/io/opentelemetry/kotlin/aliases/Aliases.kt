@file:Suppress("DEPRECATION")

package io.opentelemetry.kotlin.aliases

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.common.AttributesBuilder
import io.opentelemetry.api.internal.ImmutableSpanContext
import io.opentelemetry.api.logs.LogRecordBuilder
import io.opentelemetry.api.logs.Logger
import io.opentelemetry.api.logs.LoggerBuilder
import io.opentelemetry.api.logs.LoggerProvider
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.api.trace.SpanId
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.TraceFlags
import io.opentelemetry.api.trace.TraceId
import io.opentelemetry.api.trace.TraceState
import io.opentelemetry.api.trace.TraceStateBuilder
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.TracerBuilder
import io.opentelemetry.api.trace.TracerProvider
import io.opentelemetry.context.Context
import io.opentelemetry.context.ContextKey
import io.opentelemetry.context.ImplicitContextKeyed
import io.opentelemetry.context.Scope
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.common.Clock
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo
import io.opentelemetry.sdk.common.InstrumentationScopeInfo
import io.opentelemetry.sdk.logs.LogLimits
import io.opentelemetry.sdk.logs.LogRecordProcessor
import io.opentelemetry.sdk.logs.ReadWriteLogRecord
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder
import io.opentelemetry.sdk.logs.data.Body
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.logs.export.LogRecordExporter
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.resources.ResourceBuilder
import io.opentelemetry.sdk.trace.IdGenerator
import io.opentelemetry.sdk.trace.ReadWriteSpan
import io.opentelemetry.sdk.trace.ReadableSpan
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder
import io.opentelemetry.sdk.trace.SpanLimits
import io.opentelemetry.sdk.trace.SpanProcessor
import io.opentelemetry.sdk.trace.data.EventData
import io.opentelemetry.sdk.trace.data.LinkData
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.data.StatusData
import io.opentelemetry.sdk.trace.export.SpanExporter

typealias OtelJavaAttributes = Attributes
typealias OtelJavaAttributeKey<T> = AttributeKey<T>
typealias OtelJavaSpan = Span
typealias OtelJavaSpanContext = SpanContext
typealias OtelJavaTraceFlags = TraceFlags
typealias OtelJavaTraceState = TraceState
typealias OtelJavaSpanKind = SpanKind
typealias OtelJavaStatusCode = StatusCode
typealias OtelJavaContext = Context
typealias OtelJavaContextKey<T> = ContextKey<T>
typealias OtelJavaTracer = Tracer
typealias OtelJavaTracerProvider = TracerProvider
typealias OtelJavaClock = Clock
typealias OtelJavaImmutableSpanContext = ImmutableSpanContext
typealias OtelJavaOpenTelemetry = OpenTelemetry
typealias OtelJavaLoggerProvider = LoggerProvider
typealias OtelJavaLoggerBuilder = LoggerBuilder
typealias OtelJavaLogger = Logger
typealias OtelJavaSeverity = Severity
typealias OtelJavaCompletableResultCode = CompletableResultCode
typealias OtelJavaLogRecordData = LogRecordData
typealias OtelJavaLogRecordExporter = LogRecordExporter
typealias OtelJavaResource = Resource
typealias OtelJavaEventData = EventData
typealias OtelJavaSpanData = SpanData
typealias OtelJavaLinkData = LinkData
typealias OtelJavaStatusData = StatusData
typealias OtelJavaInstrumentationScopeInfo = InstrumentationScopeInfo
typealias OtelJavaLogRecordProcessor = LogRecordProcessor
typealias OtelJavaReadWriteLogRecord = ReadWriteLogRecord
typealias OtelJavaSpanExporter = SpanExporter
typealias OtelJavaReadWriteSpan = ReadWriteSpan
typealias OtelJavaReadableSpan = ReadableSpan
typealias OtelJavaSpanProcessor = SpanProcessor
typealias OtelJavaOpenTelemetrySdk = OpenTelemetrySdk
typealias OtelJavaSdkLoggerProvider = SdkLoggerProvider
typealias OtelJavaSdkLoggerProviderBuilder = SdkLoggerProviderBuilder
typealias OtelJavaSdkTracerProvider = SdkTracerProvider
typealias OtelJavaSdkTracerProviderBuilder = SdkTracerProviderBuilder
typealias OtelJavaBody = Body
typealias OtelJavaInstrumentationLibraryInfo = InstrumentationLibraryInfo
typealias OtelJavaAttributesBuilder = AttributesBuilder
typealias OtelJavaSpanBuilder = SpanBuilder
typealias OtelJavaContextPropagators = ContextPropagators
typealias OtelJavaImplicitContextKeyed = ImplicitContextKeyed
typealias OtelJavaScope = Scope
typealias OtelJavaTracerBuilder = TracerBuilder
typealias OtelJavaSpanId = SpanId
typealias OtelJavaTraceId = TraceId
typealias OtelJavaIdGenerator = IdGenerator
typealias OtelJavaTraceStateBuilder = TraceStateBuilder
typealias OtelJavaLogRecordBuilder = LogRecordBuilder
typealias OtelJavaResourceBuilder = ResourceBuilder
typealias OtelJavaSpanLimits = SpanLimits
typealias OtelJavaLogLimits = LogLimits
