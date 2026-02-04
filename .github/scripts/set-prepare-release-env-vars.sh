#!/bin/bash
set -euo pipefail

# Sets environment variables needed for the prepare-release-branch workflow.

mode=$1

if [[ -z "$mode" ]]; then
  echo "Error: missing mode argument (release-branch or main)" >&2
  exit 1
fi

script_dir="$(dirname "$0")"

version=$("$script_dir/get-version.sh")
echo "VERSION=$version" >> "$GITHUB_ENV"

case "$mode" in
  release-branch)
    release_branch_name=$("$script_dir/get-release-branch-name.sh" "$version")
    echo "RELEASE_BRANCH_NAME=$release_branch_name" >> "$GITHUB_ENV"
    echo "Set VERSION=$version, RELEASE_BRANCH_NAME=$release_branch_name"
    ;;
  main)
    next_version=$("$script_dir/get-next-version.sh" "$version")
    echo "NEXT_VERSION=$next_version" >> "$GITHUB_ENV"
    echo "Set VERSION=$version, NEXT_VERSION=$next_version"
    ;;
  *)
    echo "Error: invalid mode '$mode' (expected: release-branch or main)" >&2
    exit 1
    ;;
esac
