package io.opentelemetry.kotlin.metrics.view

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.metrics.instrument.InstrumentDescriptor

/**
 * The Views registered with a MeterProvider. Registration only happens at initialization, so the
 * supplied list is copied and later changes to it are ignored.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#view
 */
internal class ViewRegistry(views: List<View>) {
    private val views: List<View> = views.toList()

    /**
     * Returns every View whose selector matches, in registration order, or an empty list when the
     * instrument should use the reader defaults.
     */
    fun findViews(instrument: InstrumentDescriptor, scope: InstrumentationScopeInfo): List<View> =
        views.filter { matches(it.selector, instrument, scope) }

    /** https://opentelemetry.io/docs/specs/otel/metrics/sdk/#instrument-selection-criteria */
    private fun matches(
        selector: InstrumentSelector,
        instrument: InstrumentDescriptor,
        scope: InstrumentationScopeInfo,
    ): Boolean =
        (selector.name == null || matchesWildcard(selector.name, instrument.name)) &&
            (selector.kind == null || selector.kind == instrument.kind) &&
            (selector.unit == null || selector.unit == instrument.unit.orEmpty()) &&
            (selector.meterName == null || selector.meterName == scope.name) &&
            (selector.meterVersion == null || selector.meterVersion == scope.version) &&
            (selector.meterSchemaUrl == null || selector.meterSchemaUrl == scope.schemaUrl)

    /**
     * Matches [value] against a wildcard [pattern], ignoring case: `*` matches any run of characters
     * including none, and `?` matches exactly one.
     *
     * [star] is the position of the most recent `*` in [pattern] and [resume] is how far into [value]
     * that `*` has matched. On a mismatch the `*` absorbs one more character and matching resumes
     * after it. The function is tail recursive, so it compiles to a loop.
     */
    private tailrec fun matchesWildcard(
        pattern: String,
        value: String,
        patternIndex: Int = 0,
        valueIndex: Int = 0,
        star: Int = -1,
        resume: Int = 0,
    ): Boolean {
        val current = pattern.getOrNull(patternIndex)
        return when {
            current == '*' ->
                matchesWildcard(pattern, value, patternIndex + 1, valueIndex, patternIndex, valueIndex)
            valueIndex == value.length -> current == null
            current == '?' || current?.equals(value[valueIndex], ignoreCase = true) == true ->
                matchesWildcard(pattern, value, patternIndex + 1, valueIndex + 1, star, resume)
            star != -1 -> matchesWildcard(pattern, value, star + 1, resume + 1, star, resume + 1)
            else -> false
        }
    }
}
