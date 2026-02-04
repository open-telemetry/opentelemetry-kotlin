#!/bin/bash
set -euo pipefail

# Verifies that the current branch is a release branch

branch=${1:-$GITHUB_REF_NAME}

if [[ -z "$branch" ]]; then
  echo "Error: branch name not provided and GITHUB_REF_NAME is not set" >&2
  exit 1
fi

if [[ $branch != release/* ]]; then
  echo "Error: this workflow can only be run against release branches (current: $branch)" >&2
  exit 1
fi

echo "Verified: running on release branch '$branch'"
