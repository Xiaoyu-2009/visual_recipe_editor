package com.visual_recipe_editor.screen;

import com.visual_recipe_editor.menu.SmokerRecipeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SmokerRecipeScreen extends BaseSmeltingScreen<SmokerRecipeMenu> {
    public SmokerRecipeScreen(SmokerRecipeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "minecraft:smoking", 100, 0.35f);
    }

    @Override
    protected String getKubeJSMethod() {
        return "smoking";
    }

    @Override
    protected String getRecipeFileName() {
        return "smoker_recipe";
    }
}