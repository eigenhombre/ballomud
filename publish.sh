#!/bin/bash

set -eu

VERSION=v$(lein pprint --no-pretty -- :version)

echo "About to push $VERSION..."

docker build -t ballomud --force-rm \
       -t eigenhombre/ballomud:${VERSION} \
       -t eigenhombre/ballomud:latest .

echo
echo You should `make dockerpush` now.
echo
