# implementation

This module provides a Kotlin implementation of `api`. The entrypoint is
`createOpenTelemetry()` which returns an `OpenTelemetry` implementation that captures traces and
logs as a KMP library.

It is possible to mix & match between `compat` and `implementation`.
This can be useful if it's necessary to access certain exporters that are only defined in the Java SDK for
example. However, generally speaking library users would be encouraged to avoid mixing & matching in
the long term as this use-case has less focused testing compared to using each module individually.
