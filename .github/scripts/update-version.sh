#!/bin/bash -e

[[ -z "$1" ]] && { echo "Error: missing version argument"; exit 1; }
[[ ! -f "gradle.properties" ]] && { echo "Error: gradle.properties not found"; exit 1; }

version=$1
alpha_version=${version}-alpha

sed -Ei "s/version=.*/version=$version/" gradle.properties

