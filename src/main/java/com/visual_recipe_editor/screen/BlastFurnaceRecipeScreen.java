package com.visual_recipe_editor.screen;

import com.visual_recipe_editor.menu.BlastFurnaceRecipeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BlastFurnaceRecipeScreen extends BaseSmeltingScreen<BlastFurnaceRecipeMenu> {
    public BlastFurnaceRecipeScreen(BlastFurnaceRecipeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "minecraft:blasting", 100, 0.1f);
    }

    @Override
    protected String getKubeJSMethod() {
        return "blasting";
    }

    @Override
    protected String getRecipeFileName() {
        return "blast_furnace_recipe";
    }
}