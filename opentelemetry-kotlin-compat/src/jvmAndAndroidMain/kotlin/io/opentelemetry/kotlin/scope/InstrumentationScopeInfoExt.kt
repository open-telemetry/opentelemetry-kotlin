package io.opentelemetry.kotlin.scope

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.aliases.OtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.attributes.convertToMap

@OptIn(ExperimentalApi::class)
internal fun InstrumentationScopeInfo.toOtelJavaInstrumentationScopeInfo(): OtelJavaInstrumentationScopeInfo {
    val builder = OtelJavaInstrumentationScopeInfo.builder(name)
    version?.let(builder::setVersion)
    schemaUrl?.let(builder::setSchemaUrl)
    builder.setAttributes(attrsFromMap(attributes))
    return builder.build()
}

@OptIn(ExperimentalApi::class)
internal fun OtelJavaInstrumentationScopeInfo.toOtelKotlinInstrumentationScopeInfo(): InstrumentationScopeInfo =
    InstrumentationScopeInfoImpl(
        name = name,
        version = version,
        schemaUrl = schemaUrl,
        attributes = attributes.convertToMap()
    )
