package com.visual_recipe_editor;

import com.visual_recipe_editor.menu.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, VisualRecipeEditorMod.MODID);

    public static final RegistryObject<MenuType<CraftingRecipeMenu>> CRAFTING_TYPE = MENUS.register("crafting_recipe",
            () -> IForgeMenuType.create((windowId, inv, data) -> new CraftingRecipeMenu(windowId, inv.player)));

    public static final RegistryObject<MenuType<FurnaceRecipeMenu>> FURNACE_TYPE = MENUS.register("furnace_recipe",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FurnaceRecipeMenu(windowId, inv.player)));

    public static final RegistryObject<MenuType<BlastFurnaceRecipeMenu>> BLAST_FURNACE_TYPE = MENUS.register("blast_furnace_recipe",
            () -> IForgeMenuType.create((windowId, inv, data) -> new BlastFurnaceRecipeMenu(windowId, inv.player)));

    public static final RegistryObject<MenuType<SmokerRecipeMenu>> SMOKER_TYPE = MENUS.register("smoker_recipe",
            () -> IForgeMenuType.create((windowId, inv, data) -> new SmokerRecipeMenu(windowId, inv.player)));

    public static final RegistryObject<MenuType<SmithingRecipeMenu>> SMITHING_TYPE = MENUS.register("smithing_recipe",
            () -> IForgeMenuType.create((windowId, inv, data) -> new SmithingRecipeMenu(windowId, inv.player)));

    public static final RegistryObject<MenuType<StonecutterRecipeMenu>> STONECUTTER_TYPE = MENUS.register("stonecutter_recipe",
            () -> IForgeMenuType.create((windowId, inv, data) -> new StonecutterRecipeMenu(windowId, inv.player)));
}