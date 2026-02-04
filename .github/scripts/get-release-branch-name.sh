#!/bin/bash
set -euo pipefail

# Generates the release branch name from a version string.

version=$1

if [[ -z "$version" ]]; then
  echo "Error: missing version argument" >&2
  exit 1
fi

version_json=$("$(dirname "$0")/parse-version.sh" "$version")
major=$(echo "$version_json" | jq -r '.major')
minor=$(echo "$version_json" | jq -r '.minor')

echo "release/v$major.$minor.x"
