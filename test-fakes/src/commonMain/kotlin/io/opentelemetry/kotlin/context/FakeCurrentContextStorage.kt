package io.opentelemetry.kotlin.context
class FakeCurrentContextStorage : CurrentContextStorage {

    var context: Context = FakeContext()

    override fun attach(context: Context): Scope {
        val previous = this.context
        this.context = context
        return FakeScope {
            this.context = previous
            true
        }
    }

    override fun current(): Context = context
}
