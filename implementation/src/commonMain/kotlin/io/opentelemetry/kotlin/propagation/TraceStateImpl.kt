package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.tracing.TraceState

/**
 * Implementation of a W3C `tracestate` header.
 *
 * https://www.w3.org/TR/trace-context-2/#tracestate-header
 */
@OptIn(ExperimentalApi::class)
internal class TraceStateImpl(traceState: TraceState) {

    private val state by lazy {
        traceState.asMap()
    }

    fun encode(): String = buildString {
        state.forEach {
            if (isNotEmpty()) {
                append(LIST_MEMBER_SEPARATOR)
            }
            append(it.key)
            append(KEY_VALUE_SEPARATOR)
            append(it.value)
        }
    }

    companion object {
        private const val LIST_MEMBER_SEPARATOR = ','
        private const val KEY_VALUE_SEPARATOR = '='
        private const val OWS_SPACE = ' '
        private const val OWS_HTAB = '\t'

        fun decode(header: String, traceStateFactory: TraceStateFactory): TraceStateImpl {
            var state = traceStateFactory.default
            val seen = mutableSetOf<String>()
            val split = header.split(LIST_MEMBER_SEPARATOR)
            split.forEach { raw ->
                val parsed = parseMember(raw, seen) ?: return@forEach
                val (key, value) = parsed
                val next = state.put(key, value)
                if (next.get(key) == value) {
                    state = next
                }
            }
            return TraceStateImpl(state)
        }

        private fun parseMember(raw: String, seen: MutableSet<String>): Pair<String, String>? {
            val member = raw.trim(OWS_SPACE, OWS_HTAB)
            if (member.isEmpty()) {
                return null
            }
            val eq = member.indexOf(KEY_VALUE_SEPARATOR)
            if (eq <= 0 || eq == member.length - 1) {
                return null
            }
            val key = member.substring(0, eq)
            if (!seen.add(key)) {
                return null
            }
            return key to member.substring(eq + 1)
        }
    }
}
