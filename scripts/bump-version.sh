#!/usr/bin/env bash
# Usage: scripts/bump-version.sh <x.y.z>
# Sets the one app version (Android + iOS) in gradle.properties and syncs iosApp/Configuration/Version.xcconfig.
# Never commits.
set -euo pipefail
cd "$(dirname "$0")/.."

if ! [[ "${1:-}" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
  echo "usage: $0 <x.y.z>" >&2
  exit 1
fi
name="$1"
code=$(( BASH_REMATCH[1] * 10000 + BASH_REMATCH[2] * 100 + BASH_REMATCH[3] ))

sed -i.bak -E "s/^lexi\.versionName=.*/lexi.versionName=$name/; s/^lexi\.versionCode=.*/lexi.versionCode=$code/" gradle.properties
rm -f gradle.properties.bak
./gradlew -q --console=plain syncIosVersion

echo "Version $name (code $code)"
git --no-pager diff --stat -- gradle.properties iosApp/Configuration/Version.xcconfig
