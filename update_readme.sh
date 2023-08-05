#!/usr/bin/env sh

set -e

./gradlew run --quiet --console=plain --args="-o README.md"