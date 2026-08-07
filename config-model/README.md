# config-model

This module contains the intermediate configuration model that every configuration mechanism
targets: the programmatic DSL, environment variables, and declarative (YAML) configuration.

Every field in the model is nullable, where `null` means *unset*. Models can be merged using the
following precedence rules:

```
SDK defaults  <  (envars or declarative config file)  <  DSL
```

Envars are ignored if a declarative config file is present.

https://opentelemetry.io/docs/specs/otel/configuration/
