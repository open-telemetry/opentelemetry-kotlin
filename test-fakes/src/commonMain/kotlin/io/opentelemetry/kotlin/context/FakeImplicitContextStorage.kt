package io.opentelemetry.kotlin.context
class FakeImplicitContextStorage : ImplicitContextStorage {

    var context: Context = FakeContext()

    override fun setImplicitContext(context: Context) {
        this.context = context
    }

    override fun implicitContext(): Context = context
}
