package com.visual_recipe_editor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.visual_recipe_editor.menu.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class RecipeCommands {
    private static final String[] WORKSTATION_TYPES = {
        "crafting", "furnace", "blast_furnace", "smoker", "smithing", "stonecutter"
    };

    private static final SuggestionProvider<CommandSourceStack> WORKSTATION_SUGGESTIONS = 
        (context, builder) -> SharedSuggestionProvider.suggest(WORKSTATION_TYPES, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("recipeexporter")
                        .then(Commands.argument("workstation", StringArgumentType.string())
                                .suggests(WORKSTATION_SUGGESTIONS)
                                .executes(RecipeCommands::openWorkstationMenu))
                        .executes(context -> openWorkstationMenu(context, "crafting"))
        );

        dispatcher.register(
                Commands.literal("re")
                        .then(Commands.argument("workstation", StringArgumentType.string())
                                .suggests(WORKSTATION_SUGGESTIONS)
                                .executes(RecipeCommands::openWorkstationMenu))
                        .executes(context -> openWorkstationMenu(context, "crafting"))
        );
    }

    private static int openWorkstationMenu(CommandContext<CommandSourceStack> context) {
        String workstation = StringArgumentType.getString(context, "workstation");
        return openWorkstationMenu(context, workstation);
    }

    private static int openWorkstationMenu(CommandContext<CommandSourceStack> context, String workstation) {
        if (context.getSource().getEntity() instanceof Player player) {
            MenuProvider menuProvider = createMenuProvider(workstation);
            if (menuProvider != null) {
                player.openMenu(menuProvider);
            } else {
                player.sendSystemMessage(Component.translatable("command.visual_recipe_editor.unknown_workstation", workstation));
                player.sendSystemMessage(Component.translatable("command.visual_recipe_editor.available_types"));
            }
        }
        return 1;
    }

    private static MenuProvider createMenuProvider(String workstation) {
        return switch (workstation.toLowerCase()) {
            case "crafting", "workbench" -> new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("gui.visual_recipe_editor.crafting.title");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new CraftingRecipeMenu(id, player);
                }
            };
            case "furnace" -> new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("gui.visual_recipe_editor.furnace.title");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new FurnaceRecipeMenu(id, player);
                }
            };
            case "blast_furnace" -> new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("gui.visual_recipe_editor.blast_furnace.title");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new BlastFurnaceRecipeMenu(id, player);
                }
            };
            case "smoker" -> new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("gui.visual_recipe_editor.smoker.title");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new SmokerRecipeMenu(id, player);
                }
            };
            case "smithing" -> new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("gui.visual_recipe_editor.smithing.title");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new SmithingRecipeMenu(id, player);
                }
            };
            case "stonecutter" -> new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("gui.visual_recipe_editor.stonecutter.title");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new StonecutterRecipeMenu(id, player);
                }
            };
            default -> null;
        };
    }
}