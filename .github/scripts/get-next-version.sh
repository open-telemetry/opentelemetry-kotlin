#!/bin/bash
set -euo pipefail

# Calculates the next version after a release.

version=$1

if [[ -z "$version" ]]; then
  echo "Error: missing version argument" >&2
  exit 1
fi

version_json=$("$(dirname "$0")/parse-version.sh" "$version")
major=$(echo "$version_json" | jq -r '.major')
minor=$(echo "$version_json" | jq -r '.minor')
patch=$(echo "$version_json" | jq -r '.patch')
rc=$(echo "$version_json" | jq -r '.rc')

if [[ -n $rc ]]; then
  next_version="$major.$minor.$patch-rc.$((rc + 1))"
elif [[ $patch == 0 ]]; then
  next_version="$major.$((minor + 1)).0"
else
  echo "Error: unexpected version: $version" >&2
  echo "For patch releases (patch > 0), the version should not be incremented by this script" >&2
  exit 1
fi

echo "$next_version"
