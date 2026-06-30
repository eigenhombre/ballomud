#!/bin/bash

set -eu

VERSION=$(grep 'def version' build.clj | grep -o '"[^"]*"' | tr -d '"')

echo "About to build $VERSION..."

docker build -t ballomud --force-rm \
       -t eigenhombre/ballomud:${VERSION} \
       -t eigenhombre/ballomud:latest .
