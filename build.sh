#!/usr/bin/env bash
# Build the mod with plain javac + jar -- no Gradle, no dependency resolution.
#
# Dependencies come from a local Minecraft install, which already has the exact
# NeoForge, JEI and Immersive Engineering jars to compile against. Point these at
# your own install if they differ.
#
#   ./build.sh            -> build/mineralveinsjei-<version>.jar
set -euo pipefail

VERSION="${VERSION:-1.0.0}"
INSTANCE="${INSTANCE:-D:/Minecraft/ATLauncher/instances/TWCraft}"
LIBRARIES="${LIBRARIES:-D:/Minecraft/ATLauncher/libraries}"
# NeoForge 1.21.1 needs JDK 21. The launcher ships one, so there is usually no
# separate install to find.
JDK="${JDK:-D:/Minecraft/ATLauncher/runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta}"

cd "$(dirname "${BASH_SOURCE[0]}")"
ROOT="$PWD"
# javac here is a Windows exe: it cannot read MSYS paths like /e/Mods/..., so
# every path handed to it stays relative to this directory.
OUT="build"
CLASSES="$OUT/classes"
JAR="$OUT/mineralveinsjei-$VERSION.jar"

JAVAC="$JDK/bin/javac.exe"
JARBIN="$JDK/bin/jar.exe"
[ -x "$JAVAC" ] || { echo "no javac at $JAVAC -- set JDK to a JDK 21"; exit 1; }

rm -rf "$CLASSES" "$JAR"
mkdir -p "$CLASSES"

# javac on Windows uses ';' between classpath entries, and the list is far too
# long for a command line, so it goes in an argfile.
python tools/classpath.py "$INSTANCE" "$LIBRARIES" \
  | python -c "import sys; print('-cp \"' + ';'.join(l.strip() for l in sys.stdin if l.strip()) + '\"')" \
  > "$OUT/javac.args"
find src/main/java -name '*.java' >> "$OUT/javac.args"

"$JAVAC" -d "$CLASSES" --release 21 -encoding UTF-8 -Xlint:-options "@$OUT/javac.args"

cp -r src/main/resources/. "$CLASSES/"
# The version in mods.toml is the single source of truth for the jar name too.
sed -i "s/^version = \".*\"/version = \"$VERSION\"/" "$CLASSES/META-INF/neoforge.mods.toml"

"$JARBIN" --create --file "$JAR" -C "$CLASSES" .
echo "built $JAR"
