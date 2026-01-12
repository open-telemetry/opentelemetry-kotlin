package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.proto.common.v1.InstrumentationScope

@OptIn(ExperimentalApi::class)
fun InstrumentationScopeInfo.toProtobuf() : InstrumentationScope = InstrumentationScope(
    name = name,
    version = version ?: "",
    attributes = attributes.createKeyValues(),
)

@OptIn(ExperimentalApi::class)
internal fun Resource.toProtobuf() =
    io.opentelemetry.proto.resource.v1.Resource(attributes = attributes.createKeyValues())