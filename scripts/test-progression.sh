#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}"
mkdir -p build/progression-test
"${java_bin}javac" -encoding UTF-8 -d build/progression-test \
  src/main/java/dev/emersonoxy/rpg/AttributeStats.java \
  src/main/java/dev/emersonoxy/rpg/RpgClass.java \
  src/main/java/dev/emersonoxy/rpg/SkillTree.java \
  src/main/java/dev/emersonoxy/rpg/QuestCatalog.java \
  src/main/java/dev/emersonoxy/rpg/RpgProfile.java \
  src/main/java/dev/emersonoxy/rpg/ProfileStore.java \
  src/test/java/dev/emersonoxy/rpg/ProgressionTest.java
"${java_bin}java" -cp build/progression-test dev.emersonoxy.rpg.ProgressionTest

