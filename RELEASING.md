# Releasing opentelemetry-kotlin

## Checklist

1. Run the
   ["Draft CHANGELOG entries"](https://github.com/open-telemetry/opentelemetry-kotlin/actions/workflows/draft-change-log-entries.yaml)
   GitHub Action.
2. Create a PR updating the CHANGELOG.md and merge it after approval.
3. Run the
   ["Prepare release branch"](https://github.com/open-telemetry/opentelemetry-kotlin/actions/workflows/prepare-release-branch.yml)
   GitHub Action.
4. Merge the two PRs created by the previous workflow.
5. Run the
   ["Release"](https://github.com/open-telemetry/opentelemetry-kotlin/actions/workflows/release.yml)
   GitHub Action using the `release/vX.X.X` branch.

## Release cadence

Releases will ship at approximately a monthly cadence. Releases are subject to maintainer's discretion
and the cadence is just a convention at this stage.
