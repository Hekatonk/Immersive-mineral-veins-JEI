package com.hekatonk.mineralveinsjei;

import blusunrize.immersiveengineering.api.excavator.MineralMix;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@JeiPlugin
public class MineralVeinsJEIPlugin implements IModPlugin {
    private static final String IE = "immersiveengineering";

    /**
     * The Excavator has no craftable item, but it is a registered block item
     * (IE multiblocks drop as a single placeable item), so it works as both the
     * category icon and the catalyst. Looked up by id rather than through IE's
     * internal registry class, so this only depends on IE's api package.
     */
    static ItemStack excavator() {
        return stack("excavator");
    }

    private static ItemStack stack(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(IE, path)));
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MineralVeinsJEI.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new MineralVeinCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Veins live in the recipe manager, so they only exist once a world is
        // loaded. JEI re-runs its plugins on world join, which is when this fills.
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        List<VeinDisplay> veins = new ArrayList<>();
        for (ResourceLocation id : MineralMix.RECIPES.getRecipeNames(level)) {
            RecipeHolder<MineralMix> holder = MineralMix.RECIPES.holderById(level, id);
            if (holder != null) {
                veins.add(VeinDisplay.of(id, holder.value()));
            }
        }
        veins.sort(Comparator.comparing(vein -> vein.id().toString()));

        registration.addRecipes(MineralVeinCategory.TYPE, veins);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // The Excavator alone, the way IE's own machines do it. It is hidden from
        // the creative tabs, so it will not turn up in a JEI item search -- the
        // way into this category is looking up an ore it produces.
        ItemStack excavator = excavator();
        if (!excavator.isEmpty()) {
            registration.addRecipeCatalyst(excavator, MineralVeinCategory.TYPE);
        }
    }
}
