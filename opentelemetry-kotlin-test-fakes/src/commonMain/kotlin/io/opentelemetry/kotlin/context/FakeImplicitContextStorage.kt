package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
class FakeImplicitContextStorage : ImplicitContextStorage {

    var context: Context = FakeContext()

    override fun setImplicitContext(context: Context) {
        this.context = context
    }

    override fun implicitContext(): Context = context
}
