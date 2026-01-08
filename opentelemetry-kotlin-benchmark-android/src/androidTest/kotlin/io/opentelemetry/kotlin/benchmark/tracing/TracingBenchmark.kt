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
import io.opentelemetry.kotlin.createOpenTelemetry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalApi::class)
@RunWith(AndroidJUnit4::class)
class TracingBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun benchmarkTracerCreation() {
        val fixture = TracerCreationFixture(createOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkSpanCreation() {
        val fixture = SpanCreationFixture(createOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkSpanEnd() {
        val fixture = SpanEndFixture(createOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkComplexSpanCreation() {
        val fixture = ComplexSpanCreationFixture(createOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkAddSpanEvent() {
        val fixture = AddSpanEventFixture(createOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkAddSpanAttribute() {
        val fixture = AddSpanAttributeFixture(createOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }

    @Test
    fun benchmarkAddSpanLink() {
        val fixture = AddSpanLinkFixture(createOpenTelemetry())
        benchmarkRule.measureRepeated {
            fixture.execute()
        }
    }
}
