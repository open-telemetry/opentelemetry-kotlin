package io.opentelemetry.kotlin.benchmark.fixtures

/**
 * A test fixture that can be used for performance micro benchmarking
 */
interface BenchmarkFixture {

    /**
     * Runs the fixture
     */
    fun execute()
}
