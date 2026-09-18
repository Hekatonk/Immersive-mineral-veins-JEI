package com.hekatonk.mineralveinsjei;

import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

/** One mineral vein, flattened into exactly what the category needs to draw. */
public record VeinDisplay(
        ResourceLocation id,
        Component name,
        Component biomes,
        Component stats,
        List<Entry> ores,
        List<Entry> spoils
) {
    /** A single roll: the item, and the share of rolls that produce it. */
    public record Entry(ItemStack stack, String percent) {
    }

    /** Chances are authored to two decimals, so anything under this is noise. */
    private static final double EPSILON = 0.005;

    public static VeinDisplay of(ResourceLocation id, MineralMix mix) {
        return new VeinDisplay(
                id,
                Component.translatable(mix.getTranslationKey(id)),
                describeBiomes(mix),
                describeStats(mix),
                toEntries(mix.outputs),
                toEntries(mix.spoils)
        );
    }

    /**
     * The chances in a mineral_mix json are relative weights, not probabilities:
     * MineralMixSerializer runs StackWithChance.recalculate over both lists at
     * load, dividing by their total. So by the time a MineralMix exists its
     * chances already sum to 1, and getRandomOre just walks them in order.
     *
     * That means these values are ready to use as percentages directly -- and
     * that a vein can never have an entry the roll cannot reach.
     */
    private static List<Entry> toEntries(List<StackWithChance> source) {
        List<Entry> entries = new ArrayList<>();
        for (StackWithChance entry : source) {
            ItemStack stack = entry.stack().get();
            if (stack.isEmpty()) {
                // A tag output with nothing behind it -- a material no mod in the
                // pack provides. Its share is still allocated, so it is counted
                // as a miss rather than drawn as an empty slot.
                continue;
            }
            entries.add(new Entry(stack, percent(entry.chance() * 100.0)));
        }
        return List.copyOf(entries);
    }

    /**
     * The share of rolls that land on an entry whose tag resolves to nothing.
     * Normally zero; non-zero means the vein references a material the pack does
     * not have, and those digs come up empty.
     */
    private static double missingChance(List<StackWithChance> source) {
        double missing = 0.0;
        for (StackWithChance entry : source) {
            if (entry.stack().get().isEmpty()) {
                missing += entry.chance();
            }
        }
        return missing;
    }

    /** Slots are 20px apart, so the label under one has to stay narrow. */
    private static String percent(double value) {
        if (value > EPSILON && value < 1.0) {
            return "<1%";
        }
        return Math.round(value) + "%";
    }

    /**
     * failChance is rolled after a successful ore pick and swaps the result for
     * spoils, so it is the share of digs that come up as waste rock.
     */
    private static Component describeStats(MineralMix mix) {
        String text = "weight " + mix.weight + "  ·  "
                + trim((float) (mix.failChance * 100.0)) + "% spoils";
        double missing = missingChance(mix.outputs) * 100.0;
        if (missing > EPSILON) {
            text += "  ·  " + trim((float) missing) + "% missing";
        }
        return Component.literal(text);
    }

    private static String trim(float value) {
        String text = String.format("%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    /**
     * Biome predicates are ANDed with each other and ORed within one, which is
     * the same reading as IE's own ie.manual.entry.minerals.biomes_and/_or.
     */
    private static Component describeBiomes(MineralMix mix) {
        List<String> groups = new ArrayList<>();
        for (MineralMix.BiomeTagPredicate predicate : mix.biomeTagPredicates) {
            List<String> tags = new ArrayList<>();
            for (TagKey<Biome> tag : predicate.tags()) {
                tags.add(pretty(tag.location()));
            }
            if (!tags.isEmpty()) {
                groups.add(String.join(" or ", tags));
            }
        }
        if (groups.isEmpty()) {
            return Component.literal("Any biome");
        }
        return Component.literal(String.join(" and ", groups));
    }

    /** "minecraft:is_mountain" -> "Mountain". IE's own tags read the same way. */
    private static String pretty(ResourceLocation tag) {
        String path = tag.getPath();
        for (String prefix : new String[]{"is_", "generate_", "has_structure/"}) {
            if (path.startsWith(prefix)) {
                path = path.substring(prefix.length());
            }
        }
        StringBuilder out = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }
}
