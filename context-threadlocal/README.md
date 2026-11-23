# context-threadlocal

`createThreadLocalCurrentContextStorage()`: a `CurrentContextStorage` that confines the current
context to the thread that set it. On single-threaded targets (JS) it degrades to a single shared slot.
Pass it to the SDK with `context { storage { createThreadLocalCurrentContextStorage() } }`.

`context-coroutines` requires this storage mechanism.
