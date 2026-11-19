# opentelemetry-kotlin docs

Documentation for using opentelemetry-kotlin in an application or library.

The code samples in these docs are compiled against the current source, so they match the code on
`main`. To read the docs for a released version, browse the repository at that version's tag.

## Guides

Start with [Getting started](getting-started.md): install the SDK, record a span and a log, and
send them to a local collector.

Then read [Configuration](configuration.md): the configuration DSL, resources, exporters, sampling
and limits, environment variables, configuration files and which one wins.

To send logs, read [Logging](logging.md): emitting log records, severity, attributes, events,
linking logs to traces, and configuring processors and exporters.

Read these in order if you're new to context and propagation:

1. [Context](context.md): what a context is, the current context, context storage, threads and
   coroutines.
2. [Propagation](propagation.md): sending context between processes with propagators, carriers,
   B3 and custom propagators.

If you use opentelemetry-java alongside opentelemetry-kotlin, read [Compat mode](compat.md): the
`compat` module, converting between the Java and Kotlin APIs, and sharing context.

## Contributing to the docs

See [Code samples in docs](../CONTRIBUTING.md#code-samples-in-docs) for how samples are written
and kept in sync.
