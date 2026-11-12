# api

This module contains the main public API of the SDK as prescribed by the OTel specification in the
[API section](https://opentelemetry.io/docs/specs/otel/overview/#api). This is intended for use by
instrumentation authors and application developers.

It is intended that library consumers will spend the vast majority of their time using symbols from
this module, with the exception of the entrypoints supplied in `compat`,
`implementation`, or  `noop`.

The intention of this module is to follow the OTel specification as closely as possible. Any
syntactic sugar that diverges from the spec should live in `api-ext` instead,
so that end-users can choose whether to use extensions to the base behavior.

## Guidelines for APIs

Please see the [contributing doc](../CONTRIBUTING.md) for design guidance on APIs. In short,
use interfaces to obscure concrete types & avoid default implementations as these tend to blend
separate concerns.

## Component Stability Matrix

Until the entire API can be marked stable, we are tracking the status of each
api component here. Components marked as "stable" may still change until the 1.0 release,
but we will take great pains to minimize and eliminate breaking changes before then.
Any component marked stable here should NOT have api surface marked with `@ExperimentalApi`.

| component   | development | stable |
|-------------|-------------|--------|
| attributes  |             | ✅     |
| baggage     | ⚠️          | ️      |
| context     | ⚠️          | ️      |
| factory     | ⚠️          | ️      |
| logging     | ⚠️          | ️      |
| metrics     | ⚠️          | ️      |
| propagation | ⚠️          | ️      |
| tracing     | ⚠️          | ️      |

## Current Context API - an explanation

The [current Context API](https://opentelemetry.io/docs/specs/otel/context/#optional-global-operations)
allows for context to be applied to traces/logs automatically, rather than explicitly passing in a
`Context` parameter every time. This is a fairly tricky concept to grasp intuitively, but
essentially it composes of these parts:

1. A storage mechanism that records _what_ the current context is. There can only be one
   current context at a time in this API. The storage mechanism is fulfilled
   by the `CurrentContextStorage` API.
2. A mechanism for context objects to set themselves as the current context. This is
   achieved by calling `Context.attach()`, which stores the object in the `CurrentContextStorage`
   and returns a `Scope` object
3. When a context object no longer needs to be the current context, there needs to be a way to
   reset the current context to its previous state. A `Scope` object is a token that achieves this
   goal by calling `detach()`
4. A means of determining what the current context is at any one time. `currentContext()`
   solves this.
5. The library users calls `attach()` for contexts at appropriate units of execution in their
   application (e.g. spanning a function, or a particular workflow). Nested calls are allowed

`rootContext()` and `currentContext()` are top-level functions, so instrumentation doesn't need an
`OpenTelemetry` instance to reach them. This follows the opentelemetry-js model.

### Current Context API example usage

A code sample makes this easier to understand:

```kotlin
// Declare keys once and share the instance with whatever needs to read the value back.
// A key with a matching name is a different key - see the ContextKey docs.
private val USER_ID: ContextKey<String> = createContextKey("user.id")

internal fun exampleUsage(api: OpenTelemetry) {
    // 1. obtain the current context. Defaults to rootContext() if no current context is available.
    val ctx = currentContext()

    // 2. create a new context but don't set it as the current context.
    val newContext = ctx.set(USER_ID, "abc123")

    // 3. set the new context as the current context while the block runs. The previous context
    // is restored afterwards, even if the block throws.
    newContext.runWithContext {
        // 4. perform tracing/logging here. tracers/loggers read currentContext() internally, so
        // the span is parented to whatever span newContext holds.
        api.tracerProvider.getTracer("tracer").startSpan("my_span").end()
    }
}
```

`runWithContext` is shorthand for `attach()` followed by `detach()` in a `finally` block. Call
`attach()`/`detach()` directly when the unit of work doesn't fit inside one block.

### Context storage

There is one `CurrentContextStorage` per process. Until one is installed, `currentContext()`
returns `rootContext()` and `attach()` does nothing, so instrumentation is safe to call before
an SDK starts.

Each storage mechanism ships in its own module:

| Module                 | Factory                                     | Behavior                                    |
|------------------------|---------------------------------------------|---------------------------------------------|
| `context-single-slot`  | `createSingleSlotCurrentContextStorage()`   | One current context for the whole process. The default. |
| `context-threadlocal`  | `createThreadLocalCurrentContextStorage()`  | One current context per thread. Needed by `context-coroutines`. |

Each factory call returns a new, independent storage.

Usually the SDK installs a storage for you when it's created:

```kotlin
val otel = createOpenTelemetry {
    context { storage = createThreadLocalCurrentContextStorage() }
}
```

- If nothing is configured, the SDK installs a single-slot storage.
- If a storage is already installed, the SDK uses that one instead.
- A disabled or noop SDK installs nothing.
- `shutdown()` uninstalls the storage, but only if that SDK installed it.

You can also install a storage yourself, before creating an SDK:

```kotlin
val storage = createThreadLocalCurrentContextStorage()
installContextStorage(storage) // false if a storage is already installed
// ...
uninstallContextStorage(storage) // only removes the storage if it's this instance
```

The first install wins. `installContextStorage` wraps the storage so that misuse, such as
detaching scopes out of order, and anything the storage throws are reported instead of
propagating.

```
