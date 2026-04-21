# opentelemetry-kotlin changelog

## Unreleased

## Version 0.3.0 (2026-04-21)

- Alter Tracing API so that `Span` does not include readable values
  ([#267](https://github.com/open-telemetry/opentelemetry-kotlin/pull/267))
- Move packages of several APIs
  ([#321](https://github.com/open-telemetry/opentelemetry-kotlin/pull/321))
  ([#295](https://github.com/open-telemetry/opentelemetry-kotlin/pull/295))
  ([#293](https://github.com/open-telemetry/opentelemetry-kotlin/pull/293))
- Alter resource API
  ([#288](https://github.com/open-telemetry/opentelemetry-kotlin/pull/288))
- Add SDK instance level support of ShutdownState
  ([#300](https://github.com/open-telemetry/opentelemetry-kotlin/pull/300))
- Provide return value for Scope#detach()
  ([#287](https://github.com/open-telemetry/opentelemetry-kotlin/pull/287))
- Disallow creating span events during creation
  ([#290](https://github.com/open-telemetry/opentelemetry-kotlin/pull/290))
- Add isRemote flag to SpanContext
  ([#289](https://github.com/open-telemetry/opentelemetry-kotlin/pull/289))
- Add DSL for configuring samplers
  ([#297](https://github.com/open-telemetry/opentelemetry-kotlin/pull/297))
  ([#314](https://github.com/open-telemetry/opentelemetry-kotlin/pull/314))
  ([#325](https://github.com/open-telemetry/opentelemetry-kotlin/pull/325))
  ([#346](https://github.com/open-telemetry/opentelemetry-kotlin/pull/346))
- Capture default attributes in resource object
  ([#308](https://github.com/open-telemetry/opentelemetry-kotlin/pull/308))
  ([#317](https://github.com/open-telemetry/opentelemetry-kotlin/pull/317))
- Add ResourceFactory API
  ([#299](https://github.com/open-telemetry/opentelemetry-kotlin/pull/299))
- Bump JavaScript dependencies
  ([#309](https://github.com/open-telemetry/opentelemetry-kotlin/pull/309))
- Support exception parameter in Logger interface
  ([#296](https://github.com/open-telemetry/opentelemetry-kotlin/pull/296))
- Move createKey() function to ContextFactory
  ([#291](https://github.com/open-telemetry/opentelemetry-kotlin/pull/291))
- Implement the AlwaysOn/AlwaysOff built-in samplers
  ([#306](https://github.com/open-telemetry/opentelemetry-kotlin/pull/306))
- Move additional interfaces to sdk-api
  ([#311](https://github.com/open-telemetry/opentelemetry-kotlin/pull/311))
- Add API for span conversion
  ([#316](https://github.com/open-telemetry/opentelemetry-kotlin/pull/316))
- Support logging structured messages
  ([#313](https://github.com/open-telemetry/opentelemetry-kotlin/pull/313))
- Configure attribute limits on traces/logs
  ([#324](https://github.com/open-telemetry/opentelemetry-kotlin/pull/324))
- Allow resources to be specified at a global level
  ([#323](https://github.com/open-telemetry/opentelemetry-kotlin/pull/323))
- Alter DSL for configuring built-in samplers
  ([#327](https://github.com/open-telemetry/opentelemetry-kotlin/pull/327))
- Ensure span id is generated before sampling decisions
  ([#335](https://github.com/open-telemetry/opentelemetry-kotlin/pull/335))
- Add global attribute limits
  ([#328](https://github.com/open-telemetry/opentelemetry-kotlin/pull/328))
- Add attributes from sampling result to span
  ([#338](https://github.com/open-telemetry/opentelemetry-kotlin/pull/338))
- Remove recordException() from API
  ([#320](https://github.com/open-telemetry/opentelemetry-kotlin/pull/320))
- Ensure that TraceState complies with W3C TraceContext spec
  ([#345](https://github.com/open-telemetry/opentelemetry-kotlin/pull/345))
- Disallow overriding span status once set to Ok
  ([#357](https://github.com/open-telemetry/opentelemetry-kotlin/pull/357))
- Add bounding for OTLP response body
  ([#361](https://github.com/open-telemetry/opentelemetry-kotlin/pull/361))
- Create parent based sampler
  ([#349](https://github.com/open-telemetry/opentelemetry-kotlin/pull/349))
- Update semantic conventions to v1.40.0
  ([#363](https://github.com/open-telemetry/opentelemetry-kotlin/pull/363))
- Bump JavaScript dependency versions
  ([#360](https://github.com/open-telemetry/opentelemetry-kotlin/pull/360))
- Add ability to override span context
  ([#358](https://github.com/open-telemetry/opentelemetry-kotlin/pull/358))
- Add makeCurrent() API to ContextFactory
  ([#351](https://github.com/open-telemetry/opentelemetry-kotlin/pull/351))
- Allow specifying timeout for OTLP export
  ([#386](https://github.com/open-telemetry/opentelemetry-kotlin/pull/386))

## Version 0.2.0 (2026-03-11)

- Set explicit minCompileSdk at 34
  ([#214](https://github.com/open-telemetry/opentelemetry-kotlin/pull/214))
- Set minimum supported AGP version of 7.1.3 explicitly
  ([#219](https://github.com/open-telemetry/opentelemetry-kotlin/pull/219))
- Move various interfaces to `sdk-api` module to denote they should not be used when writing instrumentation
  ([#223](https://github.com/open-telemetry/opentelemetry-kotlin/pull/223))
  ([#224](https://github.com/open-telemetry/opentelemetry-kotlin/pull/224))
  ([#240](https://github.com/open-telemetry/opentelemetry-kotlin/pull/240))
- Various API changes to better align with the OpenTelemetry specification
  ([#225](https://github.com/open-telemetry/opentelemetry-kotlin/pull/225))
  ([#222](https://github.com/open-telemetry/opentelemetry-kotlin/pull/222))
  ([#228](https://github.com/open-telemetry/opentelemetry-kotlin/pull/228))
  ([#233](https://github.com/open-telemetry/opentelemetry-kotlin/pull/233))
  ([#235](https://github.com/open-telemetry/opentelemetry-kotlin/pull/235))
  ([#242](https://github.com/open-telemetry/opentelemetry-kotlin/pull/242))
  ([#239](https://github.com/open-telemetry/opentelemetry-kotlin/pull/239))
  ([#241](https://github.com/open-telemetry/opentelemetry-kotlin/pull/241))
  ([#238](https://github.com/open-telemetry/opentelemetry-kotlin/pull/238))
  ([#248](https://github.com/open-telemetry/opentelemetry-kotlin/pull/248))
  ([#245](https://github.com/open-telemetry/opentelemetry-kotlin/pull/245))
  ([#247](https://github.com/open-telemetry/opentelemetry-kotlin/pull/247))
  ([#246](https://github.com/open-telemetry/opentelemetry-kotlin/pull/246))
- Include semantic conventions by default as dependency of core module
  ([#249](https://github.com/open-telemetry/opentelemetry-kotlin/pull/249))
- Update opentelemetry-java to 1.60.1
  ([#260](https://github.com/open-telemetry/opentelemetry-kotlin/pull/260))

## Version 0.1.0 (2026-02-20)

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
