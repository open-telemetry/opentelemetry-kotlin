# config

This module resolves the configuration supplied by every mechanism into the single `behavior`
the SDK is initialized with, and is intended to be the only config module `implementation` and
`compat` depend on. It gathers the layers supplied by `config-dsl`, `config-envar` and
`config-yaml`, then applies the precedence rules defined in `behavior`.

This module is currently an empty placeholder for that structure.

https://opentelemetry.io/docs/specs/otel/configuration/
