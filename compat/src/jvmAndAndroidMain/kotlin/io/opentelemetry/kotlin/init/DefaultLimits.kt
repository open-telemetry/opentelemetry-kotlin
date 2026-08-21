package io.opentelemetry.kotlin.init

/**
 * Defaults for the limits the compat adapters enforce themselves. A limit left unset by every
 * configuration mechanism is not passed to the Java SDK at all, so it applies its own default to
 * everything else.
 */
internal const val DEFAULT_ATTR_LIMIT = 128
internal const val DEFAULT_LINK_LIMIT: Int = 128
internal const val DEFAULT_EVENT_LIMIT: Int = 128
