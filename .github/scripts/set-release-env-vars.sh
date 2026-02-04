#!/bin/bash
set -euo pipefail

# Sets environment variables VERSION, VERSION_PATCH, and PRIOR_VERSION to GITHUB_ENV.

script_dir="$(dirname "$0")"

version=$("$script_dir/get-version.sh")
version_json=$("$script_dir/parse-version.sh" "$version")
patch=$(echo "$version_json" | jq -r '.patch')
prior_version=$("$script_dir/get-prior-version.sh" "$version")

echo "VERSION=$version" >> "$GITHUB_ENV"
echo "VERSION_PATCH=$patch" >> "$GITHUB_ENV"
echo "PRIOR_VERSION=$prior_version" >> "$GITHUB_ENV"

echo "Set VERSION=$version, VERSION_PATCH=$patch, PRIOR_VERSION=$prior_version"
