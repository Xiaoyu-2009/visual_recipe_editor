package com.visual_recipe_editor.screen;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.visual_recipe_editor.menu.SmithingRecipeMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Files;
import java.nio.file.Path;

public class SmithingRecipeScreen extends AbstractContainerScreen<SmithingRecipeMenu> {
    private static final ResourceLocation SMITHING_LOCATION = new ResourceLocation("textures/gui/container/smithing.png");
    private Button exportJsonButton;
    private Button exportKubeJSButton;

    public SmithingRecipeScreen(SmithingRecipeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.minecraft = Minecraft.getInstance();
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 40;
        int spacing = 4;
        int startX = this.leftPos + 125;
        int startY = this.topPos + 35;

        this.exportJsonButton = Button.builder(Component.translatable("gui.visual_recipe_editor.button.json"), button -> {
                    if (!isRecipeValid()) return;
                    if (Screen.hasControlDown()) {
                        String json = generateJson();
                        Minecraft.getInstance().keyboardHandler.setClipboard(json);
                    } else {
                        exportRecipe();
                    }
                })
                .pos(startX, startY)
                .size(buttonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("gui.visual_recipe_editor.tooltip.json")))
                .build();

        this.exportKubeJSButton = Button.builder(Component.translatable("gui.visual_recipe_editor.button.kubejs"), button -> {
                    if (!isRecipeValid()) return;
                    if (Screen.hasControlDown()) {
                        String kubeJs = generateKubeJS();
                        Minecraft.getInstance().keyboardHandler.setClipboard(kubeJs);
                    } else {
                        exportKubeJSRecipe();
                    }
                })
                .pos(startX, startY + 25)
                .size(buttonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("gui.visual_recipe_editor.tooltip.kubejs")))
                .build();

        this.addRenderableWidget(exportJsonButton);
        this.addRenderableWidget(exportKubeJSButton);
        this.titleLabelX = 28;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(SMITHING_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    private boolean isRecipeValid() {
        ItemStack template = this.menu.getSlot(0).getItem();
        ItemStack base = this.menu.getSlot(1).getItem();
        ItemStack addition = this.menu.getSlot(2).getItem();
        ItemStack result = this.menu.getSlot(3).getItem();
        return !template.isEmpty() && !base.isEmpty() && !addition.isEmpty() && !result.isEmpty();
    }

    private JsonObject serializeItem(ItemStack stack) {
        JsonObject item = new JsonObject();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null) {
            item.addProperty("item", itemId.toString());
            if (stack.getCount() > 1) {
                item.addProperty("count", stack.getCount());
            }
        }
        return item;
    }

    private JsonObject serializeResult(ItemStack stack) {
        JsonObject result = new JsonObject();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null) {
            result.addProperty("id", itemId.toString());
            result.addProperty("count", stack.getCount());
        }
        return result;
    }

    private String generateJson() {
        ItemStack template = this.menu.getSlot(0).getItem();
        ItemStack base = this.menu.getSlot(1).getItem();
        ItemStack addition = this.menu.getSlot(2).getItem();
        ItemStack result = this.menu.getSlot(3).getItem();

        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:smithing_transform");
        json.add("template", serializeItem(template));
        json.add("base", serializeItem(base));
        json.add("addition", serializeItem(addition));
        json.add("result", serializeResult(result));

        return new GsonBuilder().setPrettyPrinting().create().toJson(json);
    }

    private String generateKubeJS() {
        ItemStack template = this.menu.getSlot(0).getItem();
        ItemStack base = this.menu.getSlot(1).getItem();
        ItemStack addition = this.menu.getSlot(2).getItem();
        ItemStack result = this.menu.getSlot(3).getItem();

        StringBuilder script = new StringBuilder();
        script.append("    event.smithing(\n");
        
        ResourceLocation resultId = ForgeRegistries.ITEMS.getKey(result.getItem());
        script.append("        Item.of('").append(resultId).append("'");
        if (result.getCount() > 1) {
            script.append(", ").append(result.getCount());
        }
        script.append("),\n");

        ResourceLocation templateId = ForgeRegistries.ITEMS.getKey(template.getItem());
        ResourceLocation baseId = ForgeRegistries.ITEMS.getKey(base.getItem());
        ResourceLocation additionId = ForgeRegistries.ITEMS.getKey(addition.getItem());
        
        script.append("        '").append(templateId).append("',\n");
        script.append("        '").append(baseId).append("',\n");
        script.append("        '").append(additionId).append("'\n");
        script.append("    )");

        return script.toString();
    }

    private void exportRecipe() {
        try {
            Path recipesDir = FMLPaths.GAMEDIR.get().resolve("exported_recipes");
            Files.createDirectories(recipesDir);
            Path recipePath = recipesDir.resolve("smithing_recipe_" + System.currentTimeMillis() + ".json");
            Files.writeString(recipePath, generateJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportKubeJSRecipe() {
        try {
            Path scriptsDir = FMLPaths.GAMEDIR.get().resolve("kubejs/server_scripts");
            Files.createDirectories(scriptsDir);
            Path recipePath = scriptsDir.resolve("exported_recipes.js");

            String newRecipe = generateKubeJS();
            String existingContent = "";

            if (Files.exists(recipePath)) {
                existingContent = Files.readString(recipePath);
            }

            if (!existingContent.contains("ServerEvents.recipes")) {
                existingContent = "ServerEvents.recipes(event => {\n\n});\n";
            }

            if (!existingContent.contains(newRecipe.trim())) {
                String updatedContent = existingContent.replace("});", newRecipe + "\n});");
                Files.writeString(recipePath, updatedContent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}