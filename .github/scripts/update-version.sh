#!/bin/bash
set -euo pipefail

# Updates the version in gradle.properties.

version="${1:-${VERSION:-}}"

if [[ -z "$version" ]]; then
  echo "Error: VERSION environment variable or argument is required" >&2
  exit 1
fi

if [[ ! -f "gradle.properties" ]]; then
  echo "Error: gradle.properties not found" >&2
  exit 1
fi

sed -Ei "s/version=.*/version=$version/" gradle.properties
echo "Updated gradle.properties with version $version"
