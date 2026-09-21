package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.IdGenerator

/**
 * Strategy used to generate trace and span IDs.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#id-generators
 */
@ExperimentalApi
sealed class IdGeneratorBehavior : Behavior<IdGeneratorBehavior> {

    data object Random : IdGeneratorBehavior()

    /** An [IdGenerator] supplied directly by the host application. */
    data class Custom(val supplier: () -> IdGenerator) : IdGeneratorBehavior()

    override fun mergeWith(higher: IdGeneratorBehavior): IdGeneratorBehavior = higher
}
