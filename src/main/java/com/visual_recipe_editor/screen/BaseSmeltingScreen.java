package com.visual_recipe_editor.screen;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.visual_recipe_editor.menu.BaseSmeltingMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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

public abstract class BaseSmeltingScreen<T extends BaseSmeltingMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation FURNACE_LOCATION = new ResourceLocation("textures/gui/container/furnace.png");
    private Button exportJsonButton;
    private Button exportKubeJSButton;
    private EditBox cookingTimeInput;
    private EditBox experienceInput;
    
    protected final String recipeType;
    protected final int defaultCookingTime;
    protected final float defaultExperience;

    public BaseSmeltingScreen(T menu, Inventory inventory, Component title, String recipeType, int defaultCookingTime, float defaultExperience) {
        super(menu, inventory, title);
        this.minecraft = Minecraft.getInstance();
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
        this.recipeType = recipeType;
        this.defaultCookingTime = defaultCookingTime;
        this.defaultExperience = defaultExperience;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 40;
        int spacing = 4;
        int startX = this.leftPos + 85;
        this.cookingTimeInput = new EditBox(this.font,startX - 170, this.topPos + 30,60, 20,
        Component.translatable("gui.visual_recipe_editor.input.cooking_time"));
        this.cookingTimeInput.setValue(String.valueOf(defaultCookingTime));
        this.cookingTimeInput.setTooltip(Tooltip.create(Component.translatable("gui.visual_recipe_editor.tooltip.cooking_time", defaultCookingTime)));

        this.experienceInput = new EditBox(this.font,startX - 170, this.topPos + 55,60, 20,
        Component.translatable("gui.visual_recipe_editor.input.experience"));
        this.experienceInput.setValue(String.valueOf(defaultExperience));
        this.experienceInput.setTooltip(Tooltip.create(Component.translatable("gui.visual_recipe_editor.tooltip.experience", defaultExperience)));

        this.exportJsonButton = Button.builder(Component.translatable("gui.visual_recipe_editor.button.json"), button -> {
                    if (!isRecipeValid()) return;
                    if (Screen.hasControlDown()) {
                        String json = generateJson();
                        Minecraft.getInstance().keyboardHandler.setClipboard(json);
                    } else {
                        exportRecipe();
                    }
                })
                .pos(startX, this.topPos + 58)
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
                .pos(startX + buttonWidth + spacing, this.topPos + 58)
                .size(buttonWidth, 20)
                .tooltip(Tooltip.create(Component.translatable("gui.visual_recipe_editor.tooltip.kubejs")))
                .build();

        this.addRenderableWidget(cookingTimeInput);
        this.addRenderableWidget(experienceInput);
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
        graphics.blit(FURNACE_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    private boolean isRecipeValid() {
        ItemStack input = this.menu.getSlot(0).getItem();
        ItemStack output = this.menu.getSlot(1).getItem();
        return !input.isEmpty() && !output.isEmpty();
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
        ItemStack input = this.menu.getSlot(0).getItem();
        ItemStack output = this.menu.getSlot(1).getItem();

        JsonObject json = new JsonObject();
        json.addProperty("type", recipeType);
        json.add("ingredient", serializeItem(input));
        json.add("result", serializeResult(output));

        try {
            int cookingTime = Integer.parseInt(cookingTimeInput.getValue());
            if (cookingTime != defaultCookingTime) {
                json.addProperty("cookingtime", cookingTime);
            }
        } catch (NumberFormatException ignored) {}

        try {
            float experience = Float.parseFloat(experienceInput.getValue());
            if (experience != defaultExperience) {
                json.addProperty("experience", experience);
            }
        } catch (NumberFormatException ignored) {}

        return new GsonBuilder().setPrettyPrinting().create().toJson(json);
    }

    private String generateKubeJS() {
        ItemStack input = this.menu.getSlot(0).getItem();
        ItemStack output = this.menu.getSlot(1).getItem();

        StringBuilder script = new StringBuilder();
        
        String eventMethod = getKubeJSMethod();
        script.append("    event.").append(eventMethod).append("(\n");
        
        ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(output.getItem());
        script.append("        Item.of('").append(outputId).append("'");
        if (output.getCount() > 1) {
            script.append(", ").append(output.getCount());
        }
        script.append("),\n");

        ResourceLocation inputId = ForgeRegistries.ITEMS.getKey(input.getItem());
        script.append("        '").append(inputId).append("'");

        boolean hasCustomProps = false;
        try {
            int cookingTime = Integer.parseInt(cookingTimeInput.getValue());
            if (cookingTime != defaultCookingTime) {
                if (!hasCustomProps) {
                    script.append(",\n        {\n");
                    hasCustomProps = true;
                } else {
                    script.append(",\n");
                }
                script.append("            cookingTime: ").append(cookingTime);
            }
        } catch (NumberFormatException ignored) {}

        try {
            float experience = Float.parseFloat(experienceInput.getValue());
            if (experience != defaultExperience) {
                if (!hasCustomProps) {
                    script.append(",\n        {\n");
                    hasCustomProps = true;
                } else {
                    script.append(",\n");
                }
                script.append("            experience: ").append(experience);
            }
        } catch (NumberFormatException ignored) {}

        if (hasCustomProps) {
            script.append("\n        }");
        }
        
        script.append("\n    )");

        return script.toString();
    }

    protected abstract String getKubeJSMethod();
    protected abstract String getRecipeFileName();

    private void exportRecipe() {
        try {
            Path recipesDir = FMLPaths.GAMEDIR.get().resolve("exported_recipes");
            Files.createDirectories(recipesDir);
            Path recipePath = recipesDir.resolve(getRecipeFileName() + "_" + System.currentTimeMillis() + ".json");
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (cookingTimeInput.isFocused() || experienceInput.isFocused()) {
            if (getFocused() instanceof EditBox editBox) {
                editBox.keyPressed(keyCode, scanCode, modifiers);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!cookingTimeInput.isMouseOver(mouseX, mouseY) && cookingTimeInput.isFocused()) {
            cookingTimeInput.setFocused(false);
        }
        if (!experienceInput.isMouseOver(mouseX, mouseY) && experienceInput.isFocused()) {
            experienceInput.setFocused(false);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
