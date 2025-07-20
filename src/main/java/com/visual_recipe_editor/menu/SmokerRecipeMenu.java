package com.visual_recipe_editor.menu;

import com.visual_recipe_editor.RecipeMenuTypes;
import net.minecraft.world.entity.player.Player;

public class SmokerRecipeMenu extends BaseSmeltingMenu {
    public SmokerRecipeMenu(int id, Player player) {
        super(RecipeMenuTypes.SMOKER_TYPE.get(), id, player);
    }
}