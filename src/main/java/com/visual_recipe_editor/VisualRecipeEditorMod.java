package com.visual_recipe_editor;

import com.visual_recipe_editor.command.RecipeCommands;
import com.visual_recipe_editor.screen.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(VisualRecipeEditorMod.MODID)
public class VisualRecipeEditorMod {
    public static final String MODID = "visual_recipe_editor";

    public VisualRecipeEditorMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        RecipeMenuTypes.MENUS.register(modEventBus);
        modEventBus.addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(RecipeMenuTypes.CRAFTING_TYPE.get(), CraftingRecipeScreen::new);
        MenuScreens.register(RecipeMenuTypes.FURNACE_TYPE.get(), FurnaceRecipeScreen::new);
        MenuScreens.register(RecipeMenuTypes.BLAST_FURNACE_TYPE.get(), BlastFurnaceRecipeScreen::new);
        MenuScreens.register(RecipeMenuTypes.SMOKER_TYPE.get(), SmokerRecipeScreen::new);
        MenuScreens.register(RecipeMenuTypes.SMITHING_TYPE.get(), SmithingRecipeScreen::new);
        MenuScreens.register(RecipeMenuTypes.STONECUTTER_TYPE.get(), StonecutterRecipeScreen::new);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        RecipeCommands.register(event.getDispatcher());
    }
}