package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaDoubleCounter
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleCounter
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import java.util.function.Consumer

internal class OtelJavaDoubleCounterBuilderAdapter(
    private val meter: Meter,
    private val name: String,
    private val javaMeterProvider: OtelJavaMeterProvider,
    private var unit: String? = null,
    private var description: String? = null,
) : OtelJavaDoubleCounterBuilder {

    override fun setDescription(description: String): OtelJavaDoubleCounterBuilder {
        this.description = description
        return this
    }

    override fun setUnit(unit: String): OtelJavaDoubleCounterBuilder {
        this.unit = unit
        return this
    }

    override fun build(): OtelJavaDoubleCounter = javaBuilder().build()

    override fun buildWithCallback(
        callback: Consumer<OtelJavaObservableDoubleMeasurement>,
    ): OtelJavaObservableDoubleCounter {
        val measurementCallback: (ObservableDoubleMeasurement) -> Unit = { measurement ->
            callback.accept(OtelJavaObservableDoubleMeasurementAdapter(measurement))
        }
        val counter = meter.createObservableDoubleCounter(name, unit, description, measurementCallback)
        return object : OtelJavaObservableDoubleCounter {
            override fun close() = counter.close()
        }
    }

    private fun javaBuilder() = javaMeterProvider.get("").counterBuilder(name).ofDoubles().also { builder ->
        unit?.let(builder::setUnit)
        description?.let(builder::setDescription)
    }
}
