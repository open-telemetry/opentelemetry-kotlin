package io.opentelemetry.kotlin.attributes

/**
 * A typed, immutable attribute value. There is one corresponding type per [AttributesMutator]
 * setter except for byte arrays that are implemented as an [AnyValue.BytesValue] inside [Value].
 *
 * Values are deep-copied on construction. Cases that hold strings or bytes take a `lengthLimit`
 * beyond which values are truncated to the limit. A negative limit means no limit.
 *
 * Two values are equal when they are the same case and their [toAny] forms are equal.
 *
 * https://opentelemetry.io/docs/specs/otel/common/#attribute
 */
public sealed class AttributeValue {

    /**
     * Returns the value in the form exposed by [AttributeContainer.attributes]. Primitives are
     * unwrapped, lists are returned as [List] and [Value] returns its [AnyValue].
     */
    public abstract fun toAny(): Any

    /**
     * Sets this value on [mutator] under [key] using the setter that matches its type.
     */
    public abstract fun applyTo(key: String, mutator: AttributesMutator)

    override fun equals(other: Any?): Boolean =
        other is AttributeValue && this::class == other::class && toAny() == other.toAny()

    override fun hashCode(): Int = toAny().hashCode()

    override fun toString(): String = "${this::class.simpleName}(value=${toAny()})"

    public class Str(value: String, lengthLimit: Int = NO_LIMIT) : AttributeValue() {
        public val value: String = value.truncate(lengthLimit)

        override fun toAny(): Any = value

        override fun applyTo(key: String, mutator: AttributesMutator) {
            mutator.setStringAttribute(key, value)
        }
    }

    public class Bool(public val value: Boolean) : AttributeValue() {
        override fun toAny(): Any = value

        override fun applyTo(key: String, mutator: AttributesMutator) {
            mutator.setBooleanAttribute(key, value)
        }
    }

    public class Long(public val value: kotlin.Long) : AttributeValue() {
        override fun toAny(): Any = value

        override fun applyTo(key: String, mutator: AttributesMutator) {
            mutator.setLongAttribute(key, value)
        }
    }

    public class Double(public val value: kotlin.Double) : AttributeValue() {
        override fun toAny(): Any = value

        override fun applyTo(key: String, mutator: AttributesMutator) {
            mutator.setDoubleAttribute(key, value)
        }
    }

    public class StrList(value: List<String>, lengthLimit: Int = NO_LIMIT) : AttributeValue() {
        public val value: List<String> = value.map { it.truncate(lengthLimit) }

        override fun toAny(): Any = value

        override fun applyTo(key: String, mutator: AttributesMutator) {
            mutator.setStringListAttribute(key, value)
        }
    }

    public class BoolList(value: List<Boolean>) : AttributeValue() {
        public val value: List<Boolean> = value.toList()

        override fun toAny(): Any = value

        override fun applyTo(key: String, mutator: AttributesMutator) {
            mutator.setBooleanListAttribute(key, value)
        }
    }

    public class LongList(value: List<kotlin.Long>) : AttributeValue() {
        public val value: List<kotlin.Long> = value.toList()

        override fun toAny(): Any = value

        override fun applyTo(key: String, mutator: AttributesMutator) {
            mutator.setLongListAttribute(key, value)
        }
    }

    public class DoubleList(value: List<kotlin.Double>) : AttributeValue() {
        public val value: List<kotlin.Double> = value.toList()

        override fun toAny(): Any = value

        override fun applyTo(key: String, mutator: AttributesMutator) {
            mutator.setDoubleListAttribute(key, value)
        }
    }

    /**
     * An [AnyValue], including byte arrays as [AnyValue.BytesValue].
     */
    public class Value(value: AnyValue, lengthLimit: Int = NO_LIMIT) : AttributeValue() {
        // TODO future: deep-copy and truncate nested values via AnyValue transform.
        @Suppress("MemberNameEqualsClassName") // consistent with every other case's value
        public val value: AnyValue = when (value) {
            is AnyValue.StringValue -> when (val truncated = value.value.truncate(lengthLimit)) {
                value.value -> value
                else -> AnyValue.StringValue(truncated)
            }
            is AnyValue.BytesValue -> AnyValue.BytesValue(value.value.truncatedCopy(lengthLimit))
            else -> value
        }

        override fun toAny(): Any = value

        override fun applyTo(key: String, mutator: AttributesMutator) {
            when (value) {
                is AnyValue.BytesValue -> mutator.setByteArrayAttribute(key, value.value)
                else -> mutator.setAnyValueAttribute(key, value)
            }
        }
    }

    public companion object {

        /**
         * Converts a value read from [AttributeContainer.attributes] into an [AttributeValue]. This
         * is the only place that infers an attribute's type from its runtime type, and it reproduces
         * the semantics of `setAttributes`.
         *
         * - [AttributeValue] is returned as-is
         * - [AnyValue] is wrapped in [Value].
         * - [String], [Boolean], [kotlin.Long] and [kotlin.Double] map to their own case. On JS,
         *   where number types can't be distinguished at runtime, any number other than a
         *   [kotlin.Long] is a [kotlin.Double].
         * - Other numbers are widened to [Long] when whole, and to [Double] otherwise.
         * - A [ByteArray] is copied into an [AnyValue.BytesValue].
         * - A homogeneous collection or array maps to the matching list case, with numbers widened
         *   as above. An empty one is a [StrList]. A heterogeneous one is a [StrList] of each
         *   element's toString(), where `null` renders as `"null"`.
         * - Anything else is converted via toString().
         */
        public fun fromPublic(value: Any): AttributeValue = when (value) {
            is AttributeValue -> value
            is AnyValue -> Value(value)
            is String -> Str(value)
            is Boolean -> Bool(value)
            is kotlin.Long -> Long(value)
            is kotlin.Double -> Double(value)
            is Number -> fromNumber(value)
            is ByteArray -> Value(AnyValue.BytesValue(value))
            is Collection<*> -> fromList(value.toList())
            is Array<*> -> fromList(value.toList())
            else -> Str(value.toString())
        }

        private fun fromNumber(value: Number): AttributeValue = if (value.isWholeNumber()) {
            Long(value.toLong())
        } else {
            Double(value.toDouble())
        }

        // cast after type check to avoid a second copy
        @Suppress("UNCHECKED_CAST")
        private fun fromList(values: List<*>): AttributeValue = when {
            values.all { it is String } -> StrList(values as List<String>)
            values.all { it is Boolean } -> BoolList(values as List<Boolean>)
            values.all { it is kotlin.Long } -> LongList(values as List<kotlin.Long>)
            values.all { it is kotlin.Double } -> DoubleList(values as List<kotlin.Double>)
            values.all { it is Number } -> fromNumberList(values as List<Number>)
            else -> StrList(values.map { it.toString() })
        }

        private fun fromNumberList(values: List<Number>): AttributeValue =
            if (values.all { it.isWholeNumber() }) {
                LongList(values.map { it.toLong() })
            } else {
                DoubleList(values.map { it.toDouble() })
            }

        private fun Number.isWholeNumber(): Boolean {
            val doubleValue = toDouble()
            return doubleValue.isFinite() && doubleValue == toLong().toDouble()
        }
    }
}

private const val NO_LIMIT: Int = -1

private fun String.truncate(limit: Int): String = when (limit) {
    in indices -> take(limit)
    else -> this
}

private fun ByteArray.truncatedCopy(limit: Int): ByteArray = when (limit) {
    in indices -> copyOf(limit)
    else -> copyOf()
}
