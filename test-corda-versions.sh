#!/bin/bash

for cordaUseVersion in "4.5" "4.6"; do
  echo "Run Gradle build with Corda version $cordaUseVersion"
  if ! ./gradlew clean build -x dokkaJavadoc "-PcordaVariationVersion=$cordaUseVersion" --no-daemon; then
    echo "Build for $cordaUseVersion failed, script exit with error."
    exit 1
  else
    echo "Build for $cordaUseVersion passed."
  fi
done