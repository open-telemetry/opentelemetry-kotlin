package io.opentelemetry.kotlin.config.yaml

/**
 * Returns this number as a [Long] if it is a whole number within the range of a [Long].
 */
internal fun Number.toLongIfWhole(): Long? {
    val doubleValue = toDouble()
    return toLong().takeIf { doubleValue.isFinite() && doubleValue == it.toDouble() }
}
