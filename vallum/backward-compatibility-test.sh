#!/bin/bash
set -ex

# This verifies that the latest version of Ztor works with older versions of Vallum.
# Probably there is a more flexible way of testing this using different ClassLoaders instead of

CURRENT=$(git branch --show-current)

if [[ -z $CURRENT ]]; then
    CURRENT=$(git rev-parse HEAD)
fi

mkdir -p shadowJar
gradle vallum:shadowJar
mv vallum/build/libs/vallum-dev-all.jar shadowJar/vallum-current-all.jar

git switch --detach 14c1cb1
gradle vallum:shadowJar
mv vallum/build/libs/vallum-dev-all.jar shadowJar/vallum-14c1cb1-all.jar

git switch production
gradle vallum:shadowJar
mv vallum/build/libs/vallum-dev-all.jar shadowJar/vallum-production-all.jar

git switch --detach $CURRENT
gradle vallum:test
