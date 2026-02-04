#!/bin/bash
set -euo pipefail

# Creates a pull request with the current staged changes

if [[ -z "${MODE:-}" ]]; then
  echo "Error: MODE environment variable is required (release-branch or main)" >&2
  exit 1
fi

if [[ -z "${VERSION:-}" ]]; then
  echo "Error: VERSION environment variable is required" >&2
  exit 1
fi

case "$MODE" in
  release-branch)
    if [[ -z "${RELEASE_BRANCH_NAME:-}" ]]; then
      echo "Error: RELEASE_BRANCH_NAME environment variable is required for release-branch mode" >&2
      exit 1
    fi
    message="Prepare release $VERSION"
    branch="otelbot/prepare-release-${VERSION}"
    body="$message."
    base="$RELEASE_BRANCH_NAME"
    title="[$RELEASE_BRANCH_NAME] $message"
    ;;
  main)
    if [[ -z "${NEXT_VERSION:-}" ]]; then
      echo "Error: NEXT_VERSION environment variable is required for main mode" >&2
      exit 1
    fi
    message="Update version to $NEXT_VERSION"
    branch="otelbot/update-version-to-${NEXT_VERSION}"
    body="Update version to \`$NEXT_VERSION\`."
    base="main"
    title="$message"
    ;;
  *)
    echo "Error: invalid MODE '$MODE' (expected: release-branch or main)" >&2
    exit 1
    ;;
esac

git checkout -b "$branch"
git commit -a -m "$message"
git push --set-upstream origin "$branch"
gh pr create --title "$title" \
             --body "$body" \
             --base "$base"
