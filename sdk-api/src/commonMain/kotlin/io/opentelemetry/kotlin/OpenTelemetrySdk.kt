package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.ResourceFactory

/**
 * The main entry point for the OpenTelemetry API.
 *
 * This contains interfaces in the SDK package and is not intended for use by instrumentation
 * authors: https://opentelemetry.io/docs/specs/otel/overview/#sdk
 */
@ExperimentalApi
public interface OpenTelemetrySdk : OpenTelemetry {

    /**
     * The [Clock] that will be used for obtaining timestamps by this instance.
     */
    public val clock: Clock

    /**
     * The [IdGenerator] that will be used for generating span and trace IDs.
     */
    public val idGenerator: IdGenerator

    /**
     * Allows creating and merging [Resource] instances.
     */
    public val resource: ResourceFactory
}
