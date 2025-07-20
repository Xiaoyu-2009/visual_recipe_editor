package com.visual_recipe_editor.menu;

import com.visual_recipe_editor.RecipeMenuTypes;
import net.minecraft.world.entity.player.Player;

public class BlastFurnaceRecipeMenu extends BaseSmeltingMenu {
    public BlastFurnaceRecipeMenu(int id, Player player) {
        super(RecipeMenuTypes.BLAST_FURNACE_TYPE.get(), id, player);
    }
}