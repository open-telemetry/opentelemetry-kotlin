#!/bin/bash -e

version=$("$(dirname "$0")/get-version.sh")
[[ -z "$version" ]] && { echo "Error: failed to get version" >&2; exit 1; }

if prior_version=$("$(dirname "$0")/get-prior-version.sh" "$version" 2>/dev/null); then
  range="v$prior_version..HEAD"
else
  # No prior version tag exists, use initial commit
  initial_commit=$(git rev-list --max-parents=0 HEAD)
  range="$initial_commit..HEAD"
fi

echo "## Unreleased"
echo
echo "### Migration notes"
echo
echo
echo "### 🌟 New instrumentation"
echo
echo
echo "### 📈 Enhancements"
echo
echo
echo "### 🛠️ Bug fixes"
echo
echo
echo "### 🧰 Tooling"
echo

git log --reverse \
        --perl-regexp \
        --author='^(?!renovate\[bot\] )' \
        --pretty=format:"- %s" \
        "$range" \
  | sed -E 's,\(#([0-9]+)\)$,\n  ([#\1](https://github.com/open-telemetry/opentelemetry-kotlin/pull/\1)),'