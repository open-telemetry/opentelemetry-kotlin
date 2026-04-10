# Releasing opentelemetry-kotlin

## Checklist

1. Run [draft-change-log-entries.yml](.github/workflows/draft-change-log-entries.yaml) on GitHub Actions
2. Create a PR updating the CHANGELOG.md and merge it after approval
3. Run [prepare-release-branch.yml](.github/workflows/prepare-release-branch.yml) on GitHub Actions
4. Merge the two PRs created by the previous workflow
5. Run [release.yml](.github/workflows/release.yml) on GitHub Actions using the `release/vX.X.X` branch

## Release cadence

Releases will ship at approximately a monthly cadence. Releases are subject to maintainer's discretion
and the cadence is just a convention at this stage.
