package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaDoubleCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLongCounter
import io.opentelemetry.kotlin.aliases.OtelJavaLongCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongCounter
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import java.util.function.Consumer

internal class OtelJavaLongCounterBuilderAdapter(
    private val meter: Meter,
    private val name: String,
    private val javaMeterProvider: OtelJavaMeterProvider = OtelJavaMeterProvider.noop(),
    private var unit: String? = null,
    private var description: String? = null,
) : OtelJavaLongCounterBuilder {

    override fun setDescription(description: String): OtelJavaLongCounterBuilder {
        this.description = description
        return this
    }

    override fun setUnit(unit: String): OtelJavaLongCounterBuilder {
        this.unit = unit
        return this
    }

    override fun ofDoubles(): OtelJavaDoubleCounterBuilder =
        OtelJavaDoubleCounterBuilderAdapter(meter, name, javaMeterProvider, unit, description)

    override fun build(): OtelJavaLongCounter = javaBuilder().build()

    override fun buildWithCallback(
        callback: Consumer<OtelJavaObservableLongMeasurement>,
    ): OtelJavaObservableLongCounter = javaBuilder().buildWithCallback(callback)

    private fun javaBuilder() = javaMeterProvider.get("").counterBuilder(name).also { builder ->
        unit?.let(builder::setUnit)
        description?.let(builder::setDescription)
    }
}
