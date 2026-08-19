# behavior

This module describes the behavior the SDK is configured to have. It is the intermediate
representation that every configuration mechanism targets: the programmatic DSL, environment
variables, and declarative (YAML) configuration.

Every field is nullable, where `null` means *unset*. Behaviors can be merged using the following
precedence rules:

```
SDK defaults  <  (envars or declarative config file)  <  DSL
```

Envars are ignored if a declarative config file is present.

https://opentelemetry.io/docs/specs/otel/configuration/
