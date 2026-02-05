#!/bin/bash -e

version=$1

if [[ -z "$version" ]]; then
  echo "Error: missing version argument" >&2
  exit 1
fi

if [[ $version =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)(-(rc\.([0-9]+)))?$ ]]; then
  version_major="${BASH_REMATCH[1]}"
  version_minor="${BASH_REMATCH[2]}"
  version_patch="${BASH_REMATCH[3]}"
  version_rc="${BASH_REMATCH[6]}"
else
  echo "Error: invalid version format '$version' (expected: X.Y.Z or X.Y.Z-rc.N)" >&2
  exit 1
fi

cat <<EOF
{
  "version": "$version",
  "major": "$version_major",
  "minor": "$version_minor",
  "patch": "$version_patch",
  "rc": "$version_rc"
}
EOF
