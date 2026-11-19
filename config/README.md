# config

This module resolves the configuration supplied by every mechanism into the single `behavior`
the SDK is initialized with, and is intended to be the only config module `implementation` and
`compat` depend on. It gathers the layers supplied by `config-dsl`, `config-envar` and the
declarative config file, then applies the precedence rules defined in `behavior`.

The file is read only when a reader from the opt-in `config-declarative` artifact is registered, so
`config` does not depend on `config-yaml`.

https://opentelemetry.io/docs/specs/otel/configuration/
