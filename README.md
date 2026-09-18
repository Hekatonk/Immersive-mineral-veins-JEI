# Immersive Mineral Veins JEI

Immersive Engineering ships JEI categories for fourteen of its machines, but not
for the Excavator. `immersiveengineering:mineral_mix` is a recipe type with no
viewer plugin behind it, so a pack's mineral veins are invisible to JEI and the
only in-game reference is the Engineer's Manual appendix.

This adds that one missing category.

![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)
![NeoForge](https://img.shields.io/badge/loader-NeoForge-orange)

## What it shows

A **Mineral Veins** category, one entry per vein:

- every ore with the share of digs that yield it
- the spoils the vein brings up
- the biomes it generates in
- its vein weight and its spoils chance

The Excavator is the category's catalyst, so looking up its uses lists every
vein, and looking up an ore lists the veins that produce it.

Veins are read from the loaded recipes rather than from Immersive Engineering's
defaults, so datapack and KubeJS overrides are reflected as-is.

## Notes on the numbers

The `chance` values in a `mineral_mix` json are **relative weights**, not
probabilities: `MineralMixSerializer` divides them by their total at load. The
percentages here are those normalised values, so they are what the excavator
actually rolls.

`fail_chance` is shown as **spoils**, because that is what it does -- it is
rolled after a successful ore pick and swaps the result for waste rock.

If a vein's tag output resolves to nothing -- a material no mod in the pack
provides -- its share is still allocated and those digs come up empty. That
shows as `X% missing`.

## Building

No Gradle. Dependencies come from a local Minecraft install, which already has
the exact NeoForge, JEI and Immersive Engineering jars to compile against:

```sh
./build.sh                  # -> build/mineralveinsjei-1.0.0.jar
VERSION=1.0.1 ./build.sh    # override the version
```

Point `INSTANCE`, `LIBRARIES` and `JDK` at your own install if they differ from
the defaults at the top of `build.sh`. NeoForge 1.21.1 needs JDK 21; the
launcher ships one, so there is usually nothing to install.

## Licence

MIT.
