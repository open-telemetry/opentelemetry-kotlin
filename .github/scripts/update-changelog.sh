#!/bin/bash
set -euo pipefail

# Updates the changelog for a release.

mode="${1:-${MODE:-}}"
version="${2:-${VERSION:-}}"

if [[ -z "$mode" ]]; then
  echo "Error: MODE environment variable or argument is required (release-branch or main)" >&2
  exit 1
fi

if [[ -z "$version" ]]; then
  echo "Error: VERSION environment variable or argument is required" >&2
  exit 1
fi

date=$(date "+%Y-%m-%d")

case "$mode" in
  release-branch)
    sed -Ei "s/^## Unreleased$/## Version $version ($date)/" CHANGELOG.md
    echo "Updated changelog: replaced 'Unreleased' with 'Version $version ($date)'"
    ;;
  main)
    sed -Ei "s/^## Unreleased$/## Unreleased\n\n## Version $version ($date)/" CHANGELOG.md
    echo "Updated changelog: added 'Version $version ($date)' below 'Unreleased'"
    ;;
  *)
    echo "Error: invalid mode '$mode' (expected: release-branch or main)" >&2
    exit 1
    ;;
esac
