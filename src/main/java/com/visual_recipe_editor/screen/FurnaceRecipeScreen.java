package com.visual_recipe_editor.screen;

import com.visual_recipe_editor.menu.FurnaceRecipeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FurnaceRecipeScreen extends BaseSmeltingScreen<FurnaceRecipeMenu> {
    public FurnaceRecipeScreen(FurnaceRecipeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "minecraft:smelting", 200, 0.1f);
    }

    @Override
    protected String getKubeJSMethod() {
        return "smelting";
    }

    @Override
    protected String getRecipeFileName() {
        return "furnace_recipe";
    }
}