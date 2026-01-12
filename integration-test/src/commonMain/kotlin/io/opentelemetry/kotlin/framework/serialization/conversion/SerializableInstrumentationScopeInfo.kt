package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.framework.serialization.SerializableInstrumentationScopeInfo

@OptIn(ExperimentalApi::class)
fun InstrumentationScopeInfo.toSerializable() =
    SerializableInstrumentationScopeInfo(
        name = name,
        version = version.toString(),
        schemaUrl = schemaUrl.toString(),
        attributes = attributes.toSerializable(),
    )
