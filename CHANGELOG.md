# opentelemetry-kotlin changelog

## Unreleased

- Initial donation of code to opentelemetry-kotlin. The project now uses io.opentelemetry
  coordinates
  ([#3](https://github.com/open-telemetry/opentelemetry-kotlin/pull/3))
- Removed 'opentelemetry-kotlin' prefix from all modules
  ([#17](https://github.com/open-telemetry/opentelemetry-kotlin/pull/17))
- A User-Agent header is now added to OTLP exporters
  ([#53](https://github.com/open-telemetry/opentelemetry-kotlin/pull/53))
- `SpanProcessor` now exposes `onEnding()`
  ([#50](https://github.com/open-telemetry/opentelemetry-kotlin/pull/50))
- Added stdout exporters
  ([#58](https://github.com/open-telemetry/opentelemetry-kotlin/pull/58))
- Exporters and processors now use `suspend`
  ([#64](https://github.com/open-telemetry/opentelemetry-kotlin/pull/64))
  ([#68](https://github.com/open-telemetry/opentelemetry-kotlin/pull/68))
- Added in-memory exporters for testing
  ([#54](https://github.com/open-telemetry/opentelemetry-kotlin/pull/54))
- LoggerProvider and TracerProvider now implement `shutdown()` and `forceFlush()`
  ([#86](https://github.com/open-telemetry/opentelemetry-kotlin/pull/86))
- The project now builds using AGP 9
  ([#87](https://github.com/open-telemetry/opentelemetry-kotlin/pull/87))
- Provided syntactic sugar for wrapping an operation in an implicit `Context`
  ([#89](https://github.com/open-telemetry/opentelemetry-kotlin/pull/89))
- Expose `enabled()` API on `Logger`
  ([#91](https://github.com/open-telemetry/opentelemetry-kotlin/pull/91))
- Provided syntactic sugar for tracing an operation with a `Span`
  ([#55](https://github.com/open-telemetry/opentelemetry-kotlin/pull/55))
- The `api` module has been split into `api` and `sdk-api`. `api` is intended for instrumentation
  authors; `sdk-api` is intended for application developers who instantiate OpenTelemetry. Several APIs
  have been moved to the `sdk-api` module in this release.
  ([#120](https://github.com/open-telemetry/opentelemetry-kotlin/pull/120))
- `createNoopTelemetry()` is deprecated in favor of `NoopOpenTelemetry`
  ([#130](https://github.com/open-telemetry/opentelemetry-kotlin/pull/130))

## Legacy release notes made under io.embrace coordinates

# 0.7.0

*Oct 15, 2025*

- Initial OTLP export implementation
- Various API changes and improvements

# 0.6.0

*Sep 25, 2025*

- Performance improvements and optimizations

# 0.5.1

*Sep 15, 2025*

- Fixes dependency resolution error in `opentelemetry-kotlin-implementation` module that was
  introduced in 0.5.0

# 0.5.0

*Sep 15, 2025*

- Various API changes and improvements

# 0.4.2

*Sep 4, 2025*

- Various API changes and improvements

# 0.4.1

*Aug 30, 2025*

- Various API changes and improvements

# 0.4.0

*Aug 18, 2025*

- Initial implementation of Logging + Tracing APIs

# 0.3.1

*Aug 09, 2025*

- Various API changes and fixes to the adapter implementations

# 0.3.0

*Aug 15, 2025*

- Various alterations across API surface

# 0.2.5

*July 29, 2025*

- Improve API and implementation of `Context`

# 0.2.4

*July 29, 2025*

- Miscellaneous alterations to public API surface

# 0.2.3

*July 21, 2025*

- Added more experimental interfaces to the API surface

# 0.2.2

*July 17, 2025*

- Added more experimental interfaces to the API surface

# 0.2.1

*July 10, 2025*

- Added more experimental interfaces to the API surface

# 0.2.0

*July 4, 2025*

- Added more experimental interfaces to the API surface

# 0.1.4

*June 30, 2025*

- Added more experimental interfaces to the API surface

# 0.1.3

*June 23, 2025*

- Fixes how context is set on spans

# 0.1.2

*June 20, 2025*

- Bump dependency versions used to build project
- Miscellaneous tweaks to API surface

# 0.1.1

*May 2, 2025*

- Specified explicit Kotlin toolchain of 1.8
- Miscellaneous tweaks to API surface in response to external feedback

## 0.1.0

*April 14, 2025*

- Initial release
