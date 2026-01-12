# api

This module contains the main public API of the SDK as prescribed by the OTel specification.
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

## Implicit Context API - an explanation

The [implicit Context API](https://opentelemetry.io/docs/specs/otel/context/#optional-global-operations)
allows for context to be set on traces/logs implicitly, rather than explicitly passing in a
`Context` parameter every time. This is a fairly tricky concept to grasp intuitively, but
essentially it composes of these parts:

1. A storage mechanism that records _what_ the current implicit context is. There can only be one
   implicit context at a time in this API. The storage mechanism is fulfilled
   by the `ImplicitContextStorage` API.
2. A mechanism for context objects to set themselves as the current implicit context. This is
   achieved by calling `Context.attach()`, which stores the object in the `ImplicitContextStorage`
   and returns a `Scope` object
3. When a context object no longer needs to be the implicit context, there needs to be a way to
   reset the implicit context to its previous state. A `Scope` object is a token that achieves this
   goal by calling `detach()`
4. A means of determining what the implicit context is at any one time. `implicitContext()` solves
   this.
5. The library users calls `attach()` for contexts at appropriate units of execution in their
   application (e.g. spanning a function, or a particular workflow). Nested calls are allowed

### Implicit Context API example usage

A code sample makes this easier to understand:

```
internal fun exampleUsage(api: OpenTelemetry) {
    // 1. obtain the current context. Defaults to root() if no implicit context is available.
    val ctx = api.contextFactory.implicitContext()

    // 2. create a new context but don't set it as the implicit context.
    val newContext = ctx.with(mapOf("key" to "value"))

    // 3. set the new context as the implicit context.
    val scope = newContext.attach()

    // 4. perform tracing/logging here. tracers/loggers should call implicitContext()
    // internally so the appropriate context is set automatically.
    api.getTracer("tracer").startSpan("my_span")

    // 5. unset the new context as the implicit context and restore the previous context.
    scope.detach()
}
```
