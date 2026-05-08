package io.opentelemetry.kotlin.propagation

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.tracing.TraceFlagsImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Property-based fuzz tests for [W3CTraceContextPropagator] using Kotest's
 * [io.kotest.property.Arb] generators with [checkAll]. Runs across every KMP target
 * (JVM, Android host, iOS, JS). Kotest's seed reporting + automatic shrinking surface
 * a minimal failing input on regression.
 */
@OptIn(ExperimentalApi::class)
internal class W3CTraceContextPropagatorFuzzTest {

    private val idGenerator = IdGeneratorImpl()
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory = SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory)
    private val spanFactory = SpanFactoryImpl(spanContextFactory)
    private val contextFactory = ContextFactoryImpl(spanFactory)

    private val propagator = W3CTraceContextPropagator(
        traceFlagsFactory = traceFlagsFactory,
        traceStateFactory = traceStateFactory,
        spanContextFactory = spanContextFactory,
        spanFactory = spanFactory,
    )

    private val headerArb = arbitrary { rs ->
        val len = rs.random.nextInt(0, MAX_HEADER_LEN + 1)
        buildString(len) {
            repeat(len) {
                val ch = if (rs.random.nextBoolean()) {
                    NEAR_VALID_CHARS[rs.random.nextInt(NEAR_VALID_CHARS.length)]
                } else {
                    rs.random.nextInt(PRINTABLE_ASCII_MIN, PRINTABLE_ASCII_MAX + 1).toChar()
                }
                append(ch)
            }
        }
    }

    private val traceIdArb = arbitrary { rs ->
        buildString(TRACE_ID_LEN) {
            repeat(TRACE_ID_LEN) { append(HEX_CHARS[rs.random.nextInt(HEX_CHARS.length)]) }
        }
    }.filter { id -> id.any { it != '0' } }

    private val spanIdArb = arbitrary { rs ->
        buildString(SPAN_ID_LEN) {
            repeat(SPAN_ID_LEN) { append(HEX_CHARS[rs.random.nextInt(HEX_CHARS.length)]) }
        }
    }.filter { id -> id.any { it != '0' } }

    private val traceStateEntriesArb = arbitrary { rs ->
        val seen = mutableSetOf<String>()
        val entries = mutableListOf<Pair<String, String>>()
        repeat(rs.random.nextInt(0, MAX_TRACESTATE_ENTRIES + 1)) {
            val keyLen = 1 + rs.random.nextInt(MAX_TRACESTATE_KEY_LEN)
            val key = buildString(keyLen) {
                append(LOWERCASE_LETTERS[rs.random.nextInt(LOWERCASE_LETTERS.length)])
                repeat(keyLen - 1) { append(KEY_CHARS[rs.random.nextInt(KEY_CHARS.length)]) }
            }
            if (seen.add(key)) {
                val valLen = 1 + rs.random.nextInt(MAX_TRACESTATE_VALUE_LEN)
                val value = buildString(valLen) {
                    repeat(valLen) { append(VALUE_CHARS[rs.random.nextInt(VALUE_CHARS.length)]) }
                }
                entries += key to value
            }
        }
        entries
    }

    @Test
    fun `extract does not throw for arbitrary header inputs`() = runTest {
        val root = contextFactory.root()
        checkAll(ITERATIONS, headerArb, headerArb, Arb.boolean(), Arb.boolean()) { tp, ts, hasTp, hasTs ->
            val carrier = buildMap {
                if (hasTp) {
                    put("traceparent", tp)
                }
                if (hasTs) {
                    put("tracestate", ts)
                }
            }
            val extracted = propagator.extract(root, carrier, MapTextMapGetter)
            if (extracted != root) {
                assertTrue(
                    extracted.extractSpan().spanContext.isValid,
                    "non-root context with invalid SpanContext from carrier=$carrier",
                )
            }
        }
    }

    @Test
    fun `inject extract round-trip preserves randomized valid SpanContexts`() = runTest {
        checkAll(
            ITERATIONS,
            traceIdArb,
            spanIdArb,
            Arb.boolean(),
            Arb.boolean(),
            traceStateEntriesArb,
        ) { traceId, spanId, isSampled, isRandom, entries ->
            val flags = TraceFlagsImpl(isSampled = isSampled, isRandom = isRandom)
            val state = entries.fold(traceStateFactory.default) { acc, (k, v) -> acc.put(k, v) }

            val original = spanContextFactory.create(
                traceId = traceId,
                spanId = spanId,
                traceFlags = flags,
                traceState = state,
                isRemote = false,
            )
            val context = contextFactory.root().storeSpan(spanFactory.fromSpanContext(original))

            val carrier = mutableMapOf<String, String>()
            propagator.inject(context, carrier, MapTextMapSetter)

            val extracted = propagator.extract(contextFactory.root(), carrier, MapTextMapGetter)
            val sc = extracted.extractSpan().spanContext

            assertEquals(traceId, sc.traceId)
            assertEquals(spanId, sc.spanId)
            assertEquals(flags.isSampled, sc.traceFlags.isSampled)
            assertEquals(state.asMap(), sc.traceState.asMap())
            assertTrue(sc.isRemote)
        }
    }

    private companion object {
        const val ITERATIONS = 1024
        const val MAX_HEADER_LEN = 128
        const val PRINTABLE_ASCII_MIN = 0x20
        const val PRINTABLE_ASCII_MAX = 0x7E
        const val TRACE_ID_LEN = 32
        const val SPAN_ID_LEN = 16
        const val MAX_TRACESTATE_ENTRIES = 4
        const val MAX_TRACESTATE_KEY_LEN = 16
        const val MAX_TRACESTATE_VALUE_LEN = 16
        const val HEX_CHARS = "0123456789abcdef"
        const val NEAR_VALID_CHARS = "0123456789abcdef-"
        const val LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz"
        const val KEY_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789_*/-"
        const val VALUE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!_-"
    }
}
