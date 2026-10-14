package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.FakeContext

class FakeContextFactory : ContextFactory {

    override fun root(): Context = FakeContext()

    override fun current(): Context = FakeContext()
}
