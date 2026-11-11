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

# Gradle needs a JDK; on macOS fall back to Android Studio's bundled JBR when JAVA_HOME is unset.
if [[ -z "${JAVA_HOME:-}" ]]; then
  jbr="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  if [[ -x "$jbr/bin/java" ]]; then
    export JAVA_HOME="$jbr"
  elif ! command -v java >/dev/null 2>&1 || ! java -version >/dev/null 2>&1; then
    echo "error: no JDK found; set JAVA_HOME before running this script" >&2
    exit 1
  fi
fi

# Portable in-place edit (-i.bak works with both BSD and GNU sed).
sed -i.bak -E "s/^lexi\.versionName=.*/lexi.versionName=$name/; s/^lexi\.versionCode=.*/lexi.versionCode=$code/" gradle.properties
rm -f gradle.properties.bak
./gradlew -q --console=plain syncIosVersion

echo "Version $name (code $code)"
git --no-pager diff --stat -- gradle.properties iosApp/Configuration/Version.xcconfig
