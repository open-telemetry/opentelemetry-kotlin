# Contributing to opentelemetry-kotlin

Please open an issue on GitHub with your bug report/feature request and somebody will get back to
you on whether it's something that is actively being worked on, or whether external contributions
would be accepted.

## Development guidelines

The following guidelines should be followed during development:

1. Public interfaces only belong in `api` or `api-ext`.
2. `api` aims to remain as close to the [OTel specification](https://opentelemetry.io/docs/specs/otel/) as possible.
3. Enhancements and syntactic sugar that are not part of the OTel specification should be placed in `api-ext`.
4. 1 class per source-file is preferred
5. Invalid/default values should be implemented as constants to reduce object instantiation
6. Every API should be defined as an interface (or enum/sealed class) rather than a concrete type
7. Default implementations of function signatures in interfaces is strongly discouraged, as this blends the API/implementation
8. Default values for parameters is permissible. Default values for lambda parameters should be non-complex
9. Annotate new APIs with `@ExperimentalApi` until they are considered stable
10. Platform-specific code that isn't specific to a module (e.g. getting the current time) should go in `platform-implementations` to promote reuse
