#!/usr/bin/env sh

set -e

cp pom.xml pom-local.xml

# Replace version with "local" (but don't replace version tags after depdendencies tag)
sed -i '' '0,/<dependencies>/s/<version>.*<\/version>/<version>local<\/version>/' pom-local.xml

mvn -f pom-local.xml -Dgpg.skip install
