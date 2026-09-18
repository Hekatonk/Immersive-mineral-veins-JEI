package com.hekatonk.mineralveinsjei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class MineralVeinCategory implements IRecipeCategory<VeinDisplay> {
    public static final RecipeType<VeinDisplay> TYPE =
            RecipeType.create(MineralVeinsJEI.MODID, "mineral_vein", VeinDisplay.class);

    private static final int WIDTH = 168;
    private static final int HEIGHT = 104;
    private static final int SLOT_STEP = 20;
    private static final int ORE_Y = 42;
    private static final int SPOIL_Y = 80;

    // Text colours are ARGB in 1.21 -- without the 0xFF alpha byte nothing draws.
    // JEI draws recipes on a light background, so these are dark: light greys and
    // yellows wash out completely against it.
    private static final int LABEL = 0xFF555555;
    private static final int PERCENT = 0xFF737373;

    private final IDrawable icon;

    public MineralVeinCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemStack(MineralVeinsJEIPlugin.excavator());
    }

    @Override
    public RecipeType<VeinDisplay> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.mineralveinsjei.category.mineral_vein");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, VeinDisplay vein, IFocusGroup focuses) {
        List<VeinDisplay.Entry> ores = vein.ores();
        for (int i = 0; i < ores.size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 1 + i * SLOT_STEP, ORE_Y)
                    .setStandardSlotBackground()
                    .addItemStack(ores.get(i).stack());
        }

        List<VeinDisplay.Entry> spoils = vein.spoils();
        for (int i = 0; i < spoils.size(); i++) {
            // RENDER_ONLY, not OUTPUT: spoils are cobblestone and gravel, and
            // marking them as outputs would staple this category onto the recipe
            // lookup of every filler block in the game.
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1 + i * SLOT_STEP, SPOIL_Y)
                    .setStandardSlotBackground()
                    .addItemStack(spoils.get(i).stack());
        }
    }

    @Override
    public void draw(VeinDisplay vein, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        graphics.drawString(font, vein.name(), 1, 0, LABEL, false);
        graphics.drawString(font, vein.biomes(), 1, 11, LABEL, false);
        graphics.drawString(font, vein.stats(), 1, 21, PERCENT, false);

        graphics.drawString(font, Component.translatable("gui.mineralveinsjei.ores"),
                1, 32, LABEL, false);
        drawPercentages(graphics, font, vein.ores(), ORE_Y + 18);

        graphics.drawString(font, Component.translatable("gui.mineralveinsjei.spoils"),
                1, 70, LABEL, false);
        drawPercentages(graphics, font, vein.spoils(), SPOIL_Y + 18);
    }

    private void drawPercentages(GuiGraphics graphics, Font font,
                                 List<VeinDisplay.Entry> entries, int y) {
        for (int i = 0; i < entries.size(); i++) {
            Component text = Component.literal(entries.get(i).percent());
            // Centre each label under its 16px slot.
            int x = 1 + i * SLOT_STEP + 8 - font.width(text) / 2;
            graphics.drawString(font, text, x, y, PERCENT, false);
        }
    }

    @Override
    public ResourceLocation getRegistryName(VeinDisplay vein) {
        return vein.id();
    }
}
