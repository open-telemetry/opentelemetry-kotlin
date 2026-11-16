package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Returns [value] as a limit, or `null` if it is negative, so an invalid value leaves the limit
 * unset. Zero is a valid limit.
 */
@OptIn(ExperimentalApi::class)
fun limitOrUnset(value: Int?): Int? = value?.takeIf { it >= 0 }

/**
 * Returns [value] as a limit, or `null` if it is negative or too large to represent as an [Int].
 */
@OptIn(ExperimentalApi::class)
fun limitOrUnset(value: Long?): Int? = value?.takeIf { it >= 0 && it <= Int.MAX_VALUE }?.toInt()
