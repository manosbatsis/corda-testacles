#!/bin/bash

# The website is built using MkDocs with the Material theme.
# https://squidfunk.github.io/mkdocs-material/
# It requires Python to run.
# Install the packages with the following command:
# pip install mkdocs mkdocs-material

set -ex

REPO="https://github.com/manosbatsis/corda-testacles.git"
DIR=temp-clone

# Delete any existing temporary website clone
rm -rf $DIR

# Clone the current repo into temp folder
git clone $REPO $DIR

# Move working directory into temp folder
cd $DIR

# Generate the API docs
./gradlew dokkaForGhPages

cp -R build/docs/0.x/* docs/0.x/

# Add readme as index
cat README.md > docs/index.md
# Remove redundant link
sed -i '/Checkout the documentation at/d' docs/index.md
sed -i 's~](docs/~](~g' docs/index.md
sed -i 's~.md)~)~g' docs/index.md

# Build the site and push the new files up to GitHub
mkdocs gh-deploy

# Delete our temp folder
cd ..
rm -rf $DIR
