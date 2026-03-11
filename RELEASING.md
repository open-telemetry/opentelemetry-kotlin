# Releasing opentelemetry-kotlin

## Checklist

1. Run [draft-change-log-entries.yml](.github/workflows/draft-change-log-entries.yaml) on GitHub Actions
2. Run [prepare-release-branch.yml](.github/workflows/prepare-release-branch.yml) on GitHub Actions
3. Merge the two PRs created by the previous workflow
4. Run [release.yml](.github/workflows/release.yml) on GitHub Actions using the `release/vX.X.X` branch

## Release cadence

Releases will ship at approximately a monthly cadence. Releases are subject to maintainer's discretion
and the cadence is just a convention at this stage.
