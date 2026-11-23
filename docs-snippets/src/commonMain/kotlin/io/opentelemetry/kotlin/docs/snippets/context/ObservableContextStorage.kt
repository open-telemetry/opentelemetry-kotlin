package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.Scope
import io.opentelemetry.kotlin.context.createThreadLocalCurrentContextStorage
import io.opentelemetry.kotlin.createOpenTelemetry

// region context-custom-storage
// Wraps another storage and reports every change to the current context.
class ObservableContextStorage(
    private val delegate: CurrentContextStorage,
    private val onChange: (Context) -> Unit,
) : CurrentContextStorage {

    override fun current(): Context = delegate.current()

    override fun attach(context: Context): Scope {
        val scope = delegate.attach(context)
        onChange(context)
        return object : Scope {
            override fun detach(): Boolean {
                val detached = scope.detach()
                onChange(delegate.current())
                return detached
            }
        }
    }
}

fun createSdkWithCustomStorage(): OpenTelemetry = createOpenTelemetry {
    context {
        storage {
            ObservableContextStorage(createThreadLocalCurrentContextStorage()) { ctx ->
                println("current context is now $ctx")
            }
        }
    }
}
// end-region
