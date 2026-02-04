#!/bin/bash
set -euo pipefail

# Updates the version in README.md.

version="${1:-${VERSION:-}}"

if [[ -z "$version" ]]; then
  echo "Error: VERSION environment variable or argument is required" >&2
  exit 1
fi

sed -Ei "s/(val otelKotlinVersion = \").*\"/\1${version}-alpha\"/" README.md
echo "Updated README.md with version ${version}-alpha"
