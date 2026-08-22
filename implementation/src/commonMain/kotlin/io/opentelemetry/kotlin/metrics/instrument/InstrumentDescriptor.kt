package io.opentelemetry.kotlin.metrics.instrument

/**
 * Identifying parameters captured when a Meter creates an instrument.
 *
 * Instrument names are compared case-insensitively. Kind, unit, description, and numeric value
 * type are identifying; [advice] is not, so the first advice registered for otherwise identical
 * instruments is retained by the Meter.
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

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is InstrumentDescriptor &&
            name.equals(other.name, ignoreCase = true) &&
            unit == other.unit &&
            description == other.description &&
            kind == other.kind &&
            valueType == other.valueType

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
