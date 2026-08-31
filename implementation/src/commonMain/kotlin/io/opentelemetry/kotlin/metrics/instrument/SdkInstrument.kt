package io.opentelemetry.kotlin.metrics.instrument

import io.opentelemetry.kotlin.metrics.Instrument

/**
 * Internal bridge between an API instrument and SDK measurement processing.
 *
 * Concrete SDK instruments implement this interface alongside their corresponding recording API.
 * They use [descriptor] to match Views and resolve metric streams, then forward measurements to
 * the writable storages created for those streams.
 *
 * This interface is an implementation detail; the Metrics SDK specification defines the required
 * instrument behavior but does not define an `SdkInstrument` type.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#measurement-processing
 */
internal interface SdkInstrument : Instrument {
    val descriptor: InstrumentDescriptor

    override val name: String
        get() = descriptor.name

    override val unit: String?
        get() = descriptor.unit

    override val description: String?
        get() = descriptor.description
}
