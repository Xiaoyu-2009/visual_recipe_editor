package com.visual_recipe_editor.screen;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.visual_recipe_editor.menu.CraftingRecipeMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftingRecipeScreen extends AbstractContainerScreen<CraftingRecipeMenu> {
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = new ResourceLocation(
    "textures/gui/container/crafting_table.png");
    private static final int CRAFTING_GRID_SIZE = 9;
    private static final int GRID_WIDTH = 3;
    private static final int GRID_HEIGHT = 3;
    private static final int BUTTON_WIDTH = 40;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 4;
    private static final int CHECKBOX_WIDTH = 50;
    private static final int CHECKBOX_HEIGHT = 20;
    private static final int INPUT_BOX_WIDTH = 48;
    private static final int INPUT_BOX_HEIGHT = 16;

    private Button exportJsonButton;
    private Button exportKubeJSButton;
    private Checkbox shapelessCheckbox;
    private Checkbox useNbtCheckbox;

    private boolean isShapeless = false;
    private boolean useNbt = true;

    public CraftingRecipeScreen(CraftingRecipeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.minecraft = Minecraft.getInstance();
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int startX = this.leftPos + 85;
        int checkboxX = startX - 170;

        this.shapelessCheckbox = new Checkbox(
                checkboxX, this.topPos + 30,
                CHECKBOX_WIDTH, CHECKBOX_HEIGHT,
                Component.translatable("gui.visual_recipe_editor.button.shapeless"),
                this.isShapeless) {
            @Override
            public void onPress() {
                super.onPress();
                isShapeless = this.selected();
            }
        };

        this.useNbtCheckbox = new Checkbox(
                checkboxX, this.topPos + 60,
                CHECKBOX_WIDTH, CHECKBOX_HEIGHT,
                Component.translatable("gui.visual_recipe_editor.button.use_nbt"),
                this.useNbt) {
            @Override
            public void onPress() {
                super.onPress();
                useNbt = this.selected();
            }
        };

        this.exportJsonButton = Button
                .builder(Component.translatable("gui.visual_recipe_editor.button.json"), button -> {
                    if (!isRecipeValid())
                        return;
                    if (Screen.hasControlDown()) {
                        String json = isShapeless ? generateShapelessJson() : generateJson();
                        Minecraft.getInstance().keyboardHandler.setClipboard(json);
                    } else {
                        if (isShapeless) {
                            exportShapelessRecipe();
                        } else {
                            exportRecipe();
                        }
                    }
                })
                .pos(startX, this.topPos + 58)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("gui.visual_recipe_editor.tooltip.json")))
                .build();

        this.exportKubeJSButton = Button
                .builder(Component.translatable("gui.visual_recipe_editor.button.kubejs"), button -> {
                    if (!isRecipeValid())
                        return;
                    if (Screen.hasControlDown()) {
                        String kubeJs = isShapeless ? generateShapelessKubeJS() : generateKubeJS();
                        Minecraft.getInstance().keyboardHandler.setClipboard(kubeJs);
                    } else {
                        if (isShapeless) {
                            exportShapelessKubeJSRecipe();
                        } else {
                            exportKubeJSRecipe();
                        }
                    }
                })
                .pos(startX + BUTTON_WIDTH + BUTTON_SPACING, this.topPos + 58)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("gui.visual_recipe_editor.tooltip.kubejs")))
                .build();

        this.addRenderableWidget(shapelessCheckbox);
        this.addRenderableWidget(useNbtCheckbox);
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
        graphics.blit(CRAFTING_TABLE_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    private boolean isRecipeValid() {
        boolean hasItems = false;
        boolean hasResult = !this.menu.getSlot(0).getItem().isEmpty();

        for (int i = 1; i <= CRAFTING_GRID_SIZE; i++) {
            if (!this.menu.getSlot(i).getItem().isEmpty()) {
                hasItems = true;
                break;
            }
        }

        return hasItems && hasResult;
    }

    private boolean requiresNBT(ItemStack stack) {
        if (stack.isEmpty())
            return false;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null)
            return false;

        String itemName = itemId.toString();

        return itemName.equals("minecraft:enchanted_book") ||
                itemName.equals("minecraft:potion") ||
                itemName.equals("minecraft:splash_potion") ||
                itemName.equals("minecraft:lingering_potion") ||
                itemName.equals("minecraft:tipped_arrow") ||
                itemName.contains("spawn_egg") ||
                itemName.equals("minecraft:written_book") ||
                itemName.equals("minecraft:player_head") ||
                itemName.equals("minecraft:suspicious_stew");
    }

    private JsonObject serializeItem(ItemStack stack) {
        JsonObject item = new JsonObject();

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null) {
            item.addProperty("item", itemId.toString());
        }

        if (stack.getCount() > 1) {
            item.addProperty("count", stack.getCount());
        }

        if ((useNbt || requiresNBT(stack)) && stack.hasTag()) {
            CompoundTag nbt = stack.getTag();
            if (nbt != null && !nbt.isEmpty()) {
                try {
                    JsonObject nbtJson = JsonParser.parseString(nbt.toString()).getAsJsonObject();
                    item.add("nbt", nbtJson);
                } catch (Exception ignored) {
                    item.addProperty("nbt", nbt.toString());
                }
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

        if (useNbt && stack.hasTag()) {
            CompoundTag nbt = stack.getTag();
            if (nbt != null && !nbt.isEmpty()) {
                try {
                    JsonObject nbtJson = JsonParser.parseString(nbt.toString()).getAsJsonObject();
                    result.add("nbt", nbtJson);
                } catch (Exception ignored) {
                    result.addProperty("nbt", nbt.toString());
                }
            }
        }

        return result;
    }

    private String generateJson() {
        List<ItemStack> inputs = new ArrayList<>();
        ItemStack output = this.menu.getSlot(0).getItem();

        for (int i = 1; i <= CRAFTING_GRID_SIZE; i++) {
            inputs.add(this.menu.getSlot(i).getItem());
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");

        JsonArray pattern = new JsonArray();
        JsonObject key = new JsonObject();
        char currentKey = 'A';

        for (int row = 0; row < GRID_HEIGHT; row++) {
            StringBuilder patternRow = new StringBuilder();
            for (int col = 0; col < GRID_WIDTH; col++) {
                ItemStack stack = inputs.get(row * GRID_WIDTH + col);
                if (!stack.isEmpty()) {
                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (itemId != null) {
                        key.add(String.valueOf(currentKey), serializeItem(stack));
                        patternRow.append(currentKey++);
                    }
                } else {
                    patternRow.append(" ");
                }
            }
            pattern.add(patternRow.toString());
        }

        json.add("pattern", pattern);
        json.add("key", key);
        json.add("result", serializeResult(output));

        return new GsonBuilder().setPrettyPrinting().create().toJson(json);
    }

    private String generateShapelessJson() {
        List<ItemStack> inputs = new ArrayList<>();
        ItemStack output = this.menu.getSlot(0).getItem();

        for (int i = 1; i <= CRAFTING_GRID_SIZE; i++) {
            ItemStack stack = this.menu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shapeless");

        JsonArray ingredients = new JsonArray();
        for (int i = 0; i < inputs.size(); i++) {
            ingredients.add(serializeItem(inputs.get(i)));
        }

        json.add("ingredients", ingredients);
        json.add("result", serializeResult(output));

        return new GsonBuilder().setPrettyPrinting().create().toJson(json);
    }

    private void exportRecipe() {
        try {
            Path recipesDir = FMLPaths.GAMEDIR.get().resolve("exported_recipes");
            Files.createDirectories(recipesDir);
            Path recipePath = recipesDir.resolve("crafting_recipe_" + System.currentTimeMillis() + ".json");
            Files.writeString(recipePath, generateJson());
        } catch (Exception ignored) {
        }
    }

    private void exportShapelessRecipe() {
        try {
            Path recipesDir = FMLPaths.GAMEDIR.get().resolve("exported_recipes");
            Files.createDirectories(recipesDir);
            Path recipePath = recipesDir.resolve("shapeless_recipe_" + System.currentTimeMillis() + ".json");
            Files.writeString(recipePath, generateShapelessJson());
        } catch (Exception ignored) {
        }
    }

    private String generateKubeJS() {
        List<ItemStack> inputs = new ArrayList<>();
        ItemStack output = this.menu.getSlot(0).getItem();

        for (int i = 1; i <= CRAFTING_GRID_SIZE; i++) {
            inputs.add(this.menu.getSlot(i).getItem());
        }

        StringBuilder script = new StringBuilder();
        script.append("    event.shaped(\n");

        ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(output.getItem());
        if ((useNbt || requiresNBT(output)) && output.hasTag()) {
            CompoundTag nbt = output.getTag();
            if (nbt != null && !nbt.isEmpty()) {
                script.append("        Item.of('").append(outputId).append("', ");
                if (output.getCount() > 1) {
                    script.append(output.getCount()).append(", ");
                }
                script.append("'").append(nbt.toString()).append("'),\n");
            } else {
                script.append("        Item.of('").append(outputId).append("'");
                if (output.getCount() > 1) {
                    script.append(", ").append(output.getCount());
                }
                script.append("),\n");
            }
        } else {
            script.append("        Item.of('").append(outputId).append("'");
            if (output.getCount() > 1) {
                script.append(", ").append(output.getCount());
            }
            script.append("),\n");
        }

        script.append("        [\n");

        char currentKey = 'A';
        Map<String, Character> itemToKey = new HashMap<>();

        for (int row = 0; row < GRID_HEIGHT; row++) {
            script.append("            '");
            for (int col = 0; col < GRID_WIDTH; col++) {
                ItemStack stack = inputs.get(row * GRID_WIDTH + col);
                if (!stack.isEmpty()) {
                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (itemId != null) {
                        String key;
                        key = itemId.toString();
                        if ((useNbt || requiresNBT(stack)) && stack.hasTag()) {
                            CompoundTag nbt = stack.getTag();
                            if (nbt != null && !nbt.isEmpty()) {
                                key += "|NBT:" + nbt.toString();
                            }
                        }
                        if (!itemToKey.containsKey(key)) {
                            itemToKey.put(key, currentKey++);
                        }
                        script.append(itemToKey.get(key));
                    } else {
                        script.append(" ");
                    }
                } else {
                    script.append(" ");
                }
            }
            script.append(row < GRID_HEIGHT - 1 ? "',\n" : "'\n");
        }
        script.append("        ],\n");

        script.append("        {\n");
        boolean first = true;
        for (Map.Entry<String, Character> entry : itemToKey.entrySet()) {
            if (!first)
                script.append(",\n");
            String[] keyParts = entry.getKey().split("\\|");
            String itemKey = keyParts[0];
            script.append("            ").append(entry.getValue()).append(": ");

            if (keyParts.length > 1 && keyParts[1].startsWith("NBT:")) {
                script.append("Item.of('").append(itemKey).append("', '").append(keyParts[1].substring(4))
                        .append("').strongNBT()");
            } else if (useNbt) {
                script.append("Item.of('").append(itemKey).append("', '{}')");
            } else {
                script.append("'").append(itemKey).append("'");
            }
            first = false;
        }
        script.append("\n        }\n    )");

        return script.toString();
    }

    private String generateShapelessKubeJS() {
        List<ItemStack> inputs = new ArrayList<>();
        ItemStack output = this.menu.getSlot(0).getItem();

        for (int i = 1; i <= CRAFTING_GRID_SIZE; i++) {
            ItemStack stack = this.menu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }

        StringBuilder script = new StringBuilder();
        script.append("    event.shapeless(\n");

        ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(output.getItem());
        if ((useNbt || requiresNBT(output)) && output.hasTag()) {
            CompoundTag nbt = output.getTag();
            if (nbt != null && !nbt.isEmpty()) {
                script.append("        Item.of('").append(outputId).append("', ");
                if (output.getCount() > 1) {
                    script.append(output.getCount()).append(", ");
                }
                script.append("'").append(nbt.toString()).append("'),\n");
            } else {
                script.append("        Item.of('").append(outputId).append("'");
                if (output.getCount() > 1) {
                    script.append(", ").append(output.getCount());
                }
                script.append("),\n");
            }
        } else {
            script.append("        Item.of('").append(outputId).append("'");
            if (output.getCount() > 1) {
                script.append(", ").append(output.getCount());
            }
            script.append("),\n");
        }

        script.append("        [\n");

        for (int i = 0; i < inputs.size(); i++) {
            ItemStack input = inputs.get(i);
            script.append("            ");

            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(input.getItem());

            if ((useNbt || requiresNBT(input)) && input.hasTag()) {
                CompoundTag nbt = input.getTag();
                if (nbt != null && !nbt.isEmpty()) {
                    script.append("Item.of('").append(itemId).append("', '").append(nbt.toString())
                            .append("').strongNBT()");
                } else {
                    script.append("Item.of('").append(itemId).append("', '{}')");
                }
            } else if (useNbt) {
                script.append("Item.of('").append(itemId).append("', '{}')");
            } else {
                script.append("'").append(itemId).append("'");
            }

            if (i < inputs.size() - 1) {
                script.append(",");
            }
            script.append("\n");
        }

        script.append("        ]\n    )");

        return script.toString();
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
        } catch (Exception ignored) {
        }
    }

    private void exportShapelessKubeJSRecipe() {
        try {
            Path scriptsDir = FMLPaths.GAMEDIR.get().resolve("kubejs/server_scripts");
            Files.createDirectories(scriptsDir);
            Path recipePath = scriptsDir.resolve("exported_recipes.js");

            String newRecipe = generateShapelessKubeJS();
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
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}