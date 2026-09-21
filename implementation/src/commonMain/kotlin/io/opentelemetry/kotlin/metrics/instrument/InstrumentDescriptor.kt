package io.opentelemetry.kotlin.metrics.instrument

/**
 * Identifying parameters captured when a Meter creates an instrument.
 *
 * This is a plain class rather than a data class because equality represents instrument identity
 * rather than all properties. Names are compared case-insensitively, and [advice] is excluded from
 * the identity.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument
 */
internal class InstrumentDescriptor(
    val name: String,
    val unit: String?,
    val description: String?,
    val kind: InstrumentKind,
    val valueType: InstrumentValueType,
    val advice: Advice = Advice.Empty,
) {

    /**
     * Uses case-insensitive version of [name], ignores [advice] (since advice is not part of
     * instrument identity).
     */
    override fun equals(other: Any?): Boolean =
        this === other ||
            other is InstrumentDescriptor &&
            name.equals(other.name, ignoreCase = true) &&
            unit == other.unit &&
            description == other.description &&
            kind == other.kind &&
            valueType == other.valueType

    /**
     * Uses case-insensitive version of [name], ignores [advice] (since advice is not part of
     * instrument identity).
     */
    override fun hashCode(): Int {
        var result = name.lowercase().hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + kind.hashCode()
        result = 31 * result + valueType.hashCode()
        return result
    }

    override fun toString(): String =
        "InstrumentDescriptor(name=$name, unit=$unit, description=$description, " +
            "kind=$kind, valueType=$valueType, advice=$advice)"
}
