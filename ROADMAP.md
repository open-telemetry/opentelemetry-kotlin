# OpenTelemetry Kotlin Roadmap

## How is work tracked?

opentelemetry-kotlin attempts to follow the
[OpenTelemetry specification])(https://opentelemetry.io/docs/specs/otel/) as closely as possible.
New features are tracked in
GitHub [issues](https://github.com/open-telemetry/opentelemetry-kotlin/issues)
that are grouped into
[milestones](https://github.com/open-telemetry/opentelemetry-kotlin/milestones) that represent
a common goal, such as completing a particular section of the spec.

## What if the issue tracker is missing something?

The issue tracker aims to reflect the remaining work that is required to implement the spec in full.
If there is a missing feature without a corresponding ticket, please open an issue and the
maintainers
will flesh out a milestone.

# What are the priorities of the project?

You should always feel free to pick up issues/milestones that matter to you, as different folks will
have different priorities. Here are a few of the overarching goals the SIG has agreed are
priorities,
ordered in descending priority.

## API stabilization

Many APIs are marked with `@ExperimentalApi` and are not considered stable. We need to review each
API surface and gain consensus on its design before these annotations can be removed. Context,
Logging, and Tracing are amongst the top priority APIs for this process.

## Implementation of new APIs

Several features in the specification still require implementation. The Metrics API and
Configuration API
are the most obvious examples of this.
The [milestones](https://github.com/open-telemetry/opentelemetry-kotlin/milestones) should provide
an exhaustive list of missing features.

## Declare 1.0 stability

Once the API is sufficiently stabilized then the project will declare stability and ship 1.0. After
this point breaking changes should not be shipped outside of major versions.
