#!/bin/bash

set -e

# Verify release type is present
releaseType=$1
if [ -z "$1" ]
then
  echo "Please specify release type!"
  exit 2;
fi

# Increase tag using git-semver-tag (https://github.com/timo-reymann/git-semver-tag)
tag=$(git semver-tag -level "$1" 2>&1)

# Set maven version
mvn -B versions:set -DnewVersion=$tag

rm pom.xml.versionsBackup

# Clean and deploy to central
mvn clean deploy

# Update README version
sed -i "s/<version>.*<\/version>/<version>${tag}<\/version>/g" README.md

git stage .
git commit -m "Release ${tag}"
git push
git push --tags