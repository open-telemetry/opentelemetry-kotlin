# Context

This guide explains OpenTelemetry's Context API: what a context is, how to make one "current",
how the current context is stored, and how it follows your code across threads and coroutines.

Once you're comfortable with context, the [propagation guide](propagation.md) shows how to send it
to other services.

- [Part 1: The basics](#part-1-the-basics)
    - [Why context exists](#why-context-exists)
    - [A first look](#a-first-look)
    - [Contexts are immutable](#contexts-are-immutable)
    - [Context keys](#context-keys)
    - [Spans and baggage in a context](#spans-and-baggage-in-a-context)
    - [Baggage](#baggage)
- [Part 2: The current context](#part-2-the-current-context)
    - [What "current" means](#what-current-means)
    - [runWithContext](#runwithcontext)
    - [Nesting](#nesting)
    - [attach () and detach ()](#attach-and-detach)
    - [Passing a context explicitly](#passing-a-context-explicitly)
- [Part 3: Storage, threads and coroutines](#part-3-storage-threads-and-coroutines)
    - [Context storage](#context-storage)
    - [Choosing a storage](#choosing-a-storage)
    - [Installing a storage](#installing-a-storage)
    - [Threads and executors](#threads-and-executors)
    - [Coroutines](#coroutines)
    - [Writing your own storage](#writing-your-own-storage)
- [Part 4: Compat mode](#part-4-compat-mode)
- [Part 5: Pitfalls and troubleshooting](#part-5-pitfalls-and-troubleshooting)

## Part 1: The basics

### Why context exists

Telemetry often needs to know what else is happening. When you start a span, it should be a child
of the operation that's already in progress. When you emit a log, it should be linked to the
active span.

You could pass these values as parameters through every function, but that touches a lot of code. A
**context** solves this. It's a single object that carries these values, and OpenTelemetry can
find it without you passing it around.

### A first look

A `Context` is a small key/value map. You start from the empty **root context** and add values to
it using **keys**:

```kotlin context-first-look
// A key names a value and fixes its type. Create it once and reuse it.
private val REQUEST_ID: ContextKey<String> = createContextKey("request.id")

fun contextFirstLook() {
    val root = rootContext() // the empty context
    val withId = root.set(REQUEST_ID, "req-42") // a new context. root is untouched.

    println(root.get(REQUEST_ID)) // null
    println(withId.get(REQUEST_ID)) // req-42
}
```

The functions used here (`rootContext()`, `createContextKey()`) are top-level functions in
`io.opentelemetry.kotlin.context`. They don't need an `OpenTelemetry` instance and can be called by
instrumentation at any time.

### Contexts are immutable

A context never changes after it's created. `set()` returns a **new** context that holds
everything from the old one plus the new value:

```kotlin context-immutability
private val TENANT: ContextKey<String> = createContextKey("tenant")
private val ATTEMPT: ContextKey<Int> = createContextKey("attempt")

fun contextImmutability() {
    val base = rootContext().set(TENANT, "acme")

    // set() always returns a new context, so calls can be chained.
    val first = base.set(ATTEMPT, 1)
    val second = first.set(ATTEMPT, 2) // replaces the value in the new context only

    println(first.get(ATTEMPT)) // 1
    println(second.get(ATTEMPT)) // 2
    println(second.get(TENANT)) // acme (inherited from base)

    // Setting a value to null removes it.
    val withoutTenant = second.set(TENANT, null)
    println(withoutTenant.get(TENANT)) // null
}
```

### Context keys

A `ContextKey<T>` identifies a value and fixes its type, so `get()` returns a `T?` without casts.

Keys are compared **by identity**, not by name. Every call to `createContextKey()` creates a
different key, even if you pass the same name:

```kotlin context-key-identity
// Do: declare each key once, as a shared constant.
private val FEATURE_ENABLED: ContextKey<Boolean> = createContextKey("feature.enabled")

fun contextKeyIdentity() {
    val ctx = rootContext().set(FEATURE_ENABLED, true)
    println(ctx.get(FEATURE_ENABLED)) // true

    // Don't: a second key with the same name is a different key.
    val lookalike: ContextKey<Boolean> = createContextKey("feature.enabled")
    println(ctx.get(lookalike)) // null
}
```

In practice, you should create each key once and store it somewhere that the writer and reader can
see, such as a top-level `val` or a companion object.

### Spans and baggage in a context

OpenTelemetry provides syntactic sugar to store `Span` and `Baggage` values in a context object:

| Value   | Store                                              | Read               | Remove           |
|---------|----------------------------------------------------|--------------------|------------------|
| Span    | `storeSpan(span)`, `storeSpanContext(spanContext)` | `span`             |                  |
| Baggage | `storeBaggage(baggage)`                            | `baggage`          | `clearBaggage()` |

```kotlin context-span-and-baggage
fun spanAndBaggage(otel: OpenTelemetry) {
    val span = otel.tracerProvider.getTracer("checkout").startSpan("checkout")
    val baggage = createBaggage { put("user.tier", "gold") }

    val ctx = rootContext()
        .storeSpan(span)
        .storeBaggage(baggage)

    println(ctx.span === span) // true
    println(ctx.baggage.getValue("user.tier")) // gold

    // If nothing is stored you get a safe placeholder, never null.
    val empty = rootContext()
    println(empty.span.spanContext.isValid) // false: an invalid, non-recording span
    println(empty.baggage.asMap()) // {}: empty baggage

    // clearBaggage() returns a context without any baggage.
    println(ctx.clearBaggage().baggage.asMap()) // {}

    span.end()
}
```

The read properties never return null. If nothing is stored, `span` returns an invalid,
non-recording span and `baggage` returns empty baggage.

`storeSpanContext()` wraps a `SpanContext` (just the IDs, without a live span) in a non-recording
span. This is mostly used for spans that originate from another process/machine.

### Baggage

Baggage is a set of name/value string pairs that you want to travel with a request. Typical examples
are a user tier, an experiment group or a tenant ID. Baggage is immutable and be created with
`createBaggage {}`:

```kotlin context-baggage
fun baggageBasics() {
    val baggage = createBaggage {
        put("user.id", "u-123")
        put("region", "eu-west-1", metadata = "owner=payments")
    }

    // Baggage is immutable too: put() and remove() return a new Baggage.
    val updated = baggage
        .put("region", "us-east-1")
        .remove("user.id")

    println(baggage.getValue("region")) // eu-west-1 (the original is unchanged)
    println(updated.getValue("region")) // us-east-1
    println(updated.getValue("user.id")) // null

    baggage.asMap().forEach { (name, entry) ->
        println("$name=${entry.value} metadata='${entry.metadata.value}'")
    }
}
```

Each entry can carry optional metadata, which is an opaque string that's sent along with the
value.

## Part 2: The current context

### What "current" means

To have tracers and loggers use a context automatically, you make it the **current context**.
At any moment there is exactly one current context, and `currentContext()` returns it.

When you don't pass a parent explicitly:

- `Tracer.startSpan()` uses the span in the current context as the new span's parent.
- `Logger.emit()` links the log record to the span in the current context.

If no current context exists the root context is returned instead.

### runWithContext

`runWithContext` makes a context current while a block runs, then restores the previous context:

```kotlin context-current
private val ORDER_ID: ContextKey<String> = createContextKey("order.id")

fun handleOrder(orderId: String) {
    val ctx = currentContext().set(ORDER_ID, orderId)

    ctx.runWithContext {
        chargeCard() // no extra parameter needed
    }
    // The previous context is current again here.
}

private fun chargeCard() {
    val orderId = currentContext().get(ORDER_ID)
    println("charging card for order $orderId")
}
```

`runWithContext` is the safest way to change the current context, and you should prefer it
whenever the work fits inside one block.

### Nesting

Contexts can be made current inside each other. Each `runWithContext` restores the context that
was current when it started, so the current context behaves like a stack.

The most common use is building a tree of spans. Store a span in a context, make the context
current, and every span started inside the block becomes its child:

```kotlin context-nesting
fun nesting(tracer: Tracer) {
    val parent = tracer.startSpan("parent")
    currentContext().storeSpan(parent).runWithContext {
        // startSpan() reads currentContext(), so "child" is a child of "parent".
        val child = tracer.startSpan("child")
        currentContext().storeSpan(child).runWithContext {
            tracer.startSpan("grandchild").end() // a child of "child"
        }
        child.end()
        // "parent" is the current span again here.
    }
    parent.end()
}
```

### attach () and detach ()

Sometimes work doesn't fit inside a block. It might start in one callback and finish in
another. For these cases, `attach()` makes a context current and returns a `Scope`. Calling
`detach()` on the scope restores the context that was current before `attach()`.

```kotlin context-attach-detach
// The work starts in one callback and finishes in another, so it can't fit inside one block.
class ScreenLoadTracker(private val tracer: Tracer) {
    private var span: Span? = null
    private var scope: Scope? = null

    fun onLoadStarted() {
        val newSpan = tracer.startSpan("screen-load")
        span = newSpan
        scope = currentContext().storeSpan(newSpan).attach()
    }

    fun onLoadFinished() {
        scope?.detach() // restore the previous context first...
        span?.end() // ...then end the span.
        scope = null
        span = null
    }
}
```

`runWithContext` is just `attach()` followed by `detach()` in a `finally` block. When you use
`attach()` directly, you're responsible for these rules:

- **Always detach.** A scope that's never detached leaves its context current, and every span
  started afterwards gets the wrong parent.
- **Detach in reverse order.** If you attach A and then B, detach B before A. Detaching out of
  order restores the wrong context.
- **Detach where you attached.** With thread-local storage, call `detach()` on the thread that
  called `attach()`.
- **Detach once.** Detaching a scope a second time does nothing.

The SDK reports misuse of `attach/detach` to its `SdkErrorHandler`.

### Passing a context explicitly

You don't have to make a context current to use it. Most APIs that read the current context also
accept an explicit context parameter which overrides the current context:

```kotlin context-explicit-parent
fun explicitParent(otel: OpenTelemetry, parentSpan: Span) {
    val tracer = otel.tracerProvider.getTracer("example")
    val logger = otel.loggerProvider.getLogger("example")
    val ctx = rootContext().storeSpan(parentSpan)

    // The span's parent is parentSpan, whatever the current context is.
    tracer.startSpan("child", parentContext = ctx).end()

    // The log record is linked to parentSpan.
    logger.emit(body = "child finished", context = ctx)

    // Passing the root context starts a brand new trace.
    tracer.startSpan("unrelated-work", parentContext = rootContext()).end()
}
```

The same applies to `Logger.enabled(context = ...)`.

## Part 3: Storage, threads and coroutines

### Context storage

`CurrentContextStorage` is responsible for tracking what the current context is. It acts as one
storage for the whole process, and every call to `currentContext()`, `attach()` and `runWithContext`
goes through it.

Until a storage is installed, `currentContext()` returns the root context and `attach()` does
nothing. This means instrumentation is safe to call before `CurrentContextStorage` has been set.

### Choosing a storage

OpenTelemetry Kotlin ships multiple context storage solutions in their own module:

| Module                | Factory                                    | Behavior                                      |
|-----------------------|--------------------------------------------|-----------------------------------------------|
| `context-single-slot` | `createSingleSlotCurrentContextStorage()`  | The default. One current context per process. |
| `context-threadlocal` | `createThreadLocalCurrentContextStorage()` | One current context per thread.               |

Each factory call returns a new, independent storage.

### Installing a storage

Usually a storage should be installed when the SDK is created. Configure which one in the `context` block:

```kotlin context-storage-dsl
val otel = createOpenTelemetry {
    context { storage { createThreadLocalCurrentContextStorage() } }
}
```

- If nothing is configured, the SDK installs a single-slot storage.
- If a storage is already installed, the SDK uses that one instead.
- A disabled or noop SDK installs nothing.
- `shutdown()` uninstalls the storage, but only if that SDK installed it.

You can also install a storage before an SDK instance is created:

```kotlin context-storage-manual
fun installStorage() {
    val storage = createThreadLocalCurrentContextStorage()
    installContextStorage(storage) // false if a storage is already installed
    // ...
    uninstallContextStorage(storage) // only removes the storage if it's this instance
}
```

The first install wins. `installContextStorage` wraps the storage so that misuse, such as
detaching scopes out of order, and anything the storage throws are reported to the
`SdkErrorHandler`. Each kind of problem is reported the first time it happens only, so a
repeated mistake on a hot path cannot flood the logs.

### Threads and executors

Here is an example of using the current context with thread-local storage:

```kotlin context-executor
fun runInBackground(executor: Executor, tracer: Tracer) {
    // Read the context on the calling thread...
    val ctx = currentContext()

    executor.execute {
        // ...and make it current on the worker thread.
        ctx.runWithContext {
            tracer.startSpan("background-work").end() // same parent as code on the calling thread
        }
    }
}
```

### Coroutines

A coroutine can suspend on one thread and resume on another, so a thread-local context doesn't
follow it on its own. On JVM and Android, the `context-coroutines` module provides
`asCoroutineContextElement()`. It turns a `Context` into a coroutine context element that makes the
context current every time the coroutine resumes, on whichever thread that happens, and restores
the thread's previous context every time it suspends.

```kotlin context-coroutines
// The SDK must use thread-local storage:
// createOpenTelemetry { context { storage { createThreadLocalCurrentContextStorage() } } }
suspend fun loadProfile(tracer: Tracer) {
    val span = tracer.startSpan("load-profile")
    val ctx = currentContext().storeSpan(span)

    withContext(Dispatchers.IO + ctx.asCoroutineContextElement()) {
        delay(100) // suspend, then resume, possibly on another thread

        // ctx is still current here, so this is a child of "load-profile".
        tracer.startSpan("parse-profile").end()
    }
    span.end()
}
```

The element is inherited by child coroutines. Some rules must be followed when using:

- The SDK must use thread-local storage.
- Inside a coroutine that uses the element, don't call `attach()` or `runWithContext` across a
  suspension point. When the coroutine resumes, the element restores its own context, and the
  scope you attached is lost. To change the context, start a new `withContext` with a new element.
- The element only works on JVM and Android due to technical limitations with the kotlinx.coroutines
  library.

### Writing your own storage

You can also implement `CurrentContextStorage` yourself. `current()` returns the current context,
and `attach()` makes a context current and returns a `Scope` that undoes it. This example decorates
another storage to observe every change:

```kotlin context-custom-storage
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
```

## Part 4: Compat mode

If you use the `compat` module, through `createCompatOpenTelemetry` or `toOtelKotlinApi()`, the
current context is stored in opentelemetry-java's `ContextStorage`. This allows Kotlin and Java code
to share one current context interchangeably.

This compatibility brings several required rules:

- The `context { }` block is ignored, and a warning is reported. Context storage should be configured through
  opentelemetry-java instead.
- If another storage was already installed before the compat SDK was created, it's used instead
  and a warning is reported.

It's also possible to convert a context between the Java/Kotlin APIs:

```kotlin context-compat-bridge
fun bridgeContexts(kotlinContext: Context) {
    // Hand a Kotlin context to opentelemetry-java instrumentation...
    val javaContext: JavaContext = kotlinContext.toOtelJavaContext()

    // ...and turn a Java context back into a Kotlin one, e.g. to use it as a span parent.
    val backToKotlin: Context = javaContext.toOtelKotlinContext()
    println(backToKotlin.span.spanContext.traceId)
}
```

## Part 5: Pitfalls and troubleshooting

```kotlin context-pitfalls
fun pitfalls(tracer: Tracer) {
    val span = tracer.startSpan("work")

    // Don't: storeSpan() returns a new context. On its own it doesn't make anything current.
    // currentContext().storeSpan(span)

    // Don't: attach() without detach() leaves the span current after the work has finished.
    // currentContext().storeSpan(span).attach()

    // Do: make the span current for exactly as long as the work runs.
    currentContext().storeSpan(span).runWithContext {
        doWork()
    }
    span.end()
}
```

**My spans aren't nested.** Storing a span in a context doesn't make it current. Check that you
make the context current with `runWithContext` or `attach()`, or pass it as `parentContext`.

**Spans have the wrong parent after some code runs.** A scope was probably never detached, or was
detached out of order. Prefer `runWithContext`, and look for warnings from the `SdkErrorHandler`.

**The current context is lost on a background thread.** Thread-local storage keeps the context on
the thread that set it. See [Threads and executors](#threads-and-executors).

**The context is wrong inside coroutines.** Check that the SDK uses thread-local storage and that
the coroutine uses `asCoroutineContextElement()`. See [Coroutines](#coroutines).

**`attach()` has no effect.** No storage is installed yet. This happens before the SDK is created,
after it's shut down, or when the SDK is disabled.

**`get()` returns null for a value I set.** You're probably using a different key instance with the
same name. See [Context keys](#context-keys).

**Spans have all-zero IDs.** No span processor is configured, so the SDK hands out non-recording
spans. Add an exporter in the `tracerProvider { export { } }` block.
