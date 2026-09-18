#!/usr/bin/env python3
"""Emit the compile classpath, one entry per line.

There is no Gradle here, so dependencies come from a local Minecraft install
rather than a repository: the launcher already has the exact NeoForge, JEI and
Immersive Engineering jars this mod has to compile against.

    python tools/classpath.py <instance-dir> <libraries-dir>
"""
import glob
import json
import os
import sys

MC = "1.21.1-20240808.144430"
NEOFORGE = "21.1.249"


def main():
    instance, libraries = sys.argv[1], sys.argv[2]
    entries = []

    def add(path):
        # javac argfiles treat backslash as an escape character, so a Windows
        # path only survives the trip if it is written with forward slashes.
        path = path.replace("\\", "/")
        if os.path.isfile(path) and path not in entries:
            entries.append(path)

    # Minecraft itself, NeoForge-patched and officially named.
    add(os.path.join(libraries, "net/minecraft/client", MC, "client-%s-srg.jar" % MC))
    add(os.path.join(libraries, "net/minecraft/client", MC, "client-%s-extra.jar" % MC))
    add(os.path.join(libraries, "net/neoforged/neoforge", NEOFORGE, "neoforge-%s-universal.jar" % NEOFORGE))
    add(os.path.join(libraries, "net/neoforged/neoforge", NEOFORGE, "neoforge-%s-client.jar" % NEOFORGE))

    # Everything the launcher puts on Minecraft's own classpath.
    with open(os.path.join(instance, "instance.json"), encoding="utf-8") as handle:
        for library in json.load(handle).get("libraries", []):
            artifact = library.get("downloads", {}).get("artifact")
            if artifact and artifact.get("path"):
                add(os.path.join(libraries, artifact["path"]))

    # The two mods this plugin talks to.
    for pattern in ("jei-*-neoforge-*.jar", "ImmersiveEngineering-*.jar"):
        for jar in sorted(glob.glob(os.path.join(instance, "mods", pattern))):
            add(jar)

    missing = [e for e in entries if not os.path.isfile(e)]
    if missing:
        sys.exit("missing: " + ", ".join(missing))

    print("\n".join(entries))


if __name__ == "__main__":
    main()
