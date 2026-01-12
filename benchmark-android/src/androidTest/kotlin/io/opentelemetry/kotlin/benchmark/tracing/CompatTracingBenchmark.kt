package io.opentelemetry.kotlin.benchmark.tracing

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.AddSpanAttributeFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.AddSpanEventFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.AddSpanLinkFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.ComplexSpanCreationFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.SpanCreationFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.SpanEndFixture
import io.opentelemetry.kotlin.benchmark.fixtures.tracing.TracerCreationFixture
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalApi::class)
@RunWith(AndroidJUnit4::class)
class CompatTracingBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun benchmarkTracerCreationCompat() {
        val fixture = TracerCreationFixture(createCompatOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkSpanCreationCompat() {
        val fixture = SpanCreationFixture(createCompatOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkSpanEndCompat() {
        val fixture = SpanEndFixture(createCompatOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkComplexSpanCreationCompat() {
        val fixture = ComplexSpanCreationFixture(createCompatOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkAddSpanEventCompat() {
        val fixture = AddSpanEventFixture(createCompatOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkAddSpanAttributeCompat() {
        val fixture = AddSpanAttributeFixture(createCompatOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkAddSpanLinkCompat() {
        val fixture = AddSpanLinkFixture(createCompatOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }
}
