package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Strategy used to generate trace and span IDs.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#id-generators
 */
@ExperimentalApi
sealed class IdGeneratorBehavior : Behavior<IdGeneratorBehavior> {

    data object Random : IdGeneratorBehavior()

    override fun mergeWith(higher: IdGeneratorBehavior): IdGeneratorBehavior = higher
}
