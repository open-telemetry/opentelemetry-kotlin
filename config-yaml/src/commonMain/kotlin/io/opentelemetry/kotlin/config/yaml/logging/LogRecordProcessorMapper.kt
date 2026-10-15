package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.LogRecordProcessor
import io.opentelemetry.kotlin.config.yaml.toBehavior

/**
 * Maps`logger_provider.processors` onto processor behavior.
 */
@ExperimentalApi
fun List<LogRecordProcessor>.toBehavior(): LogRecordProcessorBehavior? {
    // TODO: Add support to multiple exporters being in use simultaneously once we have the LogRecordProcessor
    //  fully implemented.
    for ((batch, simple) in this) {
        when {
            batch != null -> return LogRecordProcessorBehavior(
                batch = batch.toBehavior()
            )
            simple != null -> return LogRecordProcessorBehavior(
                simple = simple.toBehavior()
            )
        }
    }

    return null
}
