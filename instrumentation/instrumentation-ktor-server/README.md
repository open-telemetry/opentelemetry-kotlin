# instrumentation-ktor-server

This module instruments the [Ktor server](https://ktor.io/docs/server-create-a-new-project.html).
It records a `SERVER` span for each inbound request.

JVM and Android only, as Ktor's server engines do not support the iOS or JS targets.
