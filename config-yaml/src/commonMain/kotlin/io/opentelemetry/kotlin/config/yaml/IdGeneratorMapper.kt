package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import io.opentelemetry.kotlin.config.schema.model.IdGenerator

/**
 * Maps the `tracer_provider.id_generator` section of a declarative config file onto the behavior it
 * supplies. An omitted strategy is left unset so the SDK default can apply.
 */
@ExperimentalApi
fun IdGenerator.toBehavior(): IdGeneratorBehavior? =
    random?.let { IdGeneratorBehavior.Random }
