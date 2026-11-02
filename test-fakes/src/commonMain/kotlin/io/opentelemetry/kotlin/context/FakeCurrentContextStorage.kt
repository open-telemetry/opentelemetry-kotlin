package io.opentelemetry.kotlin.context
class FakeCurrentContextStorage : CurrentContextStorage {

    var context: Context = FakeContext()

    override fun setCurrent(context: Context) {
        this.context = context
    }

    override fun current(): Context = context
}
