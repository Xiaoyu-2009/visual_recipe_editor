package com.visual_recipe_editor.menu;

import com.visual_recipe_editor.RecipeMenuTypes;
import net.minecraft.world.entity.player.Player;

public class FurnaceRecipeMenu extends BaseSmeltingMenu {
    public FurnaceRecipeMenu(int id, Player player) {
        super(RecipeMenuTypes.FURNACE_TYPE.get(), id, player);
    }
}