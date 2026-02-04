#!/bin/bash
set -euo pipefail

# Verifies prerequisites for running prepare-release-branch workflow

branch=${1:-$GITHUB_REF_NAME}

if [[ -z "$branch" ]]; then
  echo "Error: branch name not provided and GITHUB_REF_NAME is not set" >&2
  exit 1
fi

if [[ $branch != main ]]; then
  echo "Error: this workflow should only be run against main (current: $branch)" >&2
  exit 1
fi

if ! grep --quiet "^## Unreleased$" CHANGELOG.md; then
  echo "Error: the changelog is missing an \"Unreleased\" section" >&2
  exit 1
fi

echo "Verified: prerequisites met for prepare-release-branch"
