#!/bin/bash -e

[[ ! -f "gradle.properties" ]] && { echo "Error: gradle.properties not found" >&2; exit 1; }

version=$(grep ^version= gradle.properties | sed s/version=// | tr -d '\r')
[[ -z "$version" ]] && { echo "Error: version not found in gradle.properties" >&2; exit 1; }

echo "$version"
