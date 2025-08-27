package com.visual_recipe_editor.menu;

import com.visual_recipe_editor.RecipeMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SmithingRecipeMenu extends AbstractContainerMenu {
    protected final Container container;
    protected final Player player;

    public SmithingRecipeMenu(int id, Player player) {
        super(RecipeMenuTypes.SMITHING_TYPE.get(), id);
        this.container = new SimpleContainer(4);
        this.player = player;

        this.addSlot(new Slot(this.container, 0, 8, 48));
        this.addSlot(new Slot(this.container, 1, 26, 48));
        this.addSlot(new Slot(this.container, 2, 44, 48));
        this.addSlot(new Slot(this.container, 3, 98, 48) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });

        for(int k = 0; k < 3; k++) {
            for(int i1 = 0; i1 < 9; i1++) {
                this.addSlot(new Slot(player.getInventory(), i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for(int l = 0; l < 9; l++) {
            this.addSlot(new Slot(player.getInventory(), l, 8 + l * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.container);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index == 3) {
                if (!this.moveItemStackTo(slotStack, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 4 && index < 31) {
                if (!this.moveItemStackTo(slotStack, 31, 40, false) &&
                    !this.moveItemStackTo(slotStack, 0, 3, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 31 && index < 40) {
                if (!this.moveItemStackTo(slotStack, 4, 31, false) &&
                    !this.moveItemStackTo(slotStack, 0, 3, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 4, 40, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
            broadcastChanges();
        }
        return itemstack;
    }

    public Container getContainer() {
        return this.container;
    }
}