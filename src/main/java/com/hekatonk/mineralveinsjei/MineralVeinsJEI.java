package com.hekatonk.mineralveinsjei;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/**
 * Immersive Engineering ships JEI categories for fourteen of its machines, but
 * none for the Excavator: {@code immersiveengineering:mineral_mix} is a recipe
 * type with no viewer plugin behind it, so a pack's mineral veins are invisible
 * to JEI and the only in-game reference is the Engineer's Manual appendix.
 *
 * <p>This mod adds that one missing category. It reads the veins that are
 * actually loaded, so it reflects datapack overrides rather than IE's defaults.
 */
@Mod(value = MineralVeinsJEI.MODID, dist = Dist.CLIENT)
public class MineralVeinsJEI {
    public static final String MODID = "mineralveinsjei";

    public MineralVeinsJEI() {
    }
}
