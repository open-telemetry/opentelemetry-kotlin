# api

This module contains the main public API of the SDK as prescribed by the OTel specification in the
[API section](https://opentelemetry.io/docs/specs/otel/overview/#api). This is intended for use by
instrumentation authors and application developers.

It is intended that library consumers will spend the vast majority of their time using symbols from
this module, with the exception of the entrypoints supplied in `compat`,
`implementation`, or  `noop`.

The intention of this module is to follow the OTel specification as closely as possible. Any
syntactic sugar that diverges from the spec should live in `api-ext` instead,
so that end-users can choose whether to use extensions to the base behavior.

## Guidelines for APIs

Please see the [contributing doc](../CONTRIBUTING.md) for design guidance on APIs. In short,
use interfaces to obscure concrete types & avoid default implementations as these tend to blend
separate concerns.

## Component Stability Matrix

Until the entire API can be marked stable, we are tracking the status of each
api component here. Components marked as "stable" may still change until the 1.0 release,
but we will take great pains to minimize and eliminate breaking changes before then.
Any component marked stable here should NOT have api surface marked with `@ExperimentalApi`.

| component   | development | stable |
|-------------|-------------|--------|
| attributes  |             | ✅     |
| baggage     | ⚠️          | ️      |
| context     | ⚠️          | ️      |
| factory     | ⚠️          | ️      |
| logging     | ⚠️          | ️      |
| metrics     | ⚠️          | ️      |
| propagation | ⚠️          | ️      |
| tracing     | ⚠️          | ️      |

## Context and propagation

See the [context guide](../docs/context.md) and the [propagation guide](../docs/propagation.md).
