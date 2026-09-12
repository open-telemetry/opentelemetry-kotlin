package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.framework.serialization.SerializableInstrumentationScopeInfo

fun InstrumentationScopeInfo.toSerializable() =
    SerializableInstrumentationScopeInfo(
        name = name,
        version = version.toString(),
        schemaUrl = schemaUrl.toString(),
        attributes = attributes.toSerializable(),
    )
