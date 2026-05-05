package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Represents an attribute value as defined by the OpenTelemetry specification.
 *
 * https://opentelemetry.io/docs/specs/otel/common/#anyvalue
 */
@ExperimentalApi
public sealed class AnyValue {

    public object NullValue : AnyValue() {
        override fun toString(): String = "NullValue"
    }

    public class StringValue(public val value: String) : AnyValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is StringValue) {
                return false
            }
            return value == other.value
        }

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = "StringValue(value=$value)"
    }

    public class BoolValue(public val value: Boolean) : AnyValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is BoolValue) {
                return false
            }
            return value == other.value
        }

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = "BoolValue(value=$value)"
    }

    public class LongValue(public val value: Long) : AnyValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is LongValue) {
                return false
            }
            return value == other.value
        }

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = "LongValue(value=$value)"
    }

    public class DoubleValue(public val value: Double) : AnyValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is DoubleValue) {
                return false
            }
            return value == other.value
        }

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = "DoubleValue(value=$value)"
    }

    public class BytesValue(public val value: ByteArray) : AnyValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is BytesValue) {
                return false
            }
            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int = value.contentHashCode()

        override fun toString(): String = "BytesValue(size=${value.size})"
    }

    public class ListValue(public val values: List<AnyValue>) : AnyValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is ListValue) {
                return false
            }
            return values == other.values
        }

        override fun hashCode(): Int = values.hashCode()

        override fun toString(): String = "ListValue(values=$values)"
    }

    public class MapValue(public val values: Map<String, AnyValue>) : AnyValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other !is MapValue) {
                return false
            }
            return values == other.values
        }

        override fun hashCode(): Int = values.hashCode()

        override fun toString(): String = "MapValue(values=$values)"
    }
}
