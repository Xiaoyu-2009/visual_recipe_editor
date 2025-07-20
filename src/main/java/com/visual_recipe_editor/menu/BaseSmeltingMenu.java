package com.visual_recipe_editor.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class BaseSmeltingMenu extends AbstractContainerMenu {
    protected final Container container;
    protected final Player player;

    protected BaseSmeltingMenu(MenuType<?> menuType, int id, Player player) {
        super(menuType, id);
        this.container = new SimpleContainer(2);
        this.player = player;

        // 输入槽位
        this.addSlot(new Slot(this.container, 0, 56, 35));
        // 输出槽位
        this.addSlot(new Slot(this.container, 1, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });

        // 玩家背包
        for(int k = 0; k < 3; k++) {
            for(int i1 = 0; i1 < 9; i1++) {
                this.addSlot(new Slot(player.getInventory(), i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        // 玩家快捷栏
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

            if (index == 1) { // 输出槽位
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index != 0) { // 不是输入槽位
                if (index >= 2 && index < 29) { // 主背包
                    if (!this.moveItemStackTo(slotStack, 29, 38, false) &&
                            !this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 29 && index < 38) { // 快捷栏
                    if (!this.moveItemStackTo(slotStack, 2, 29, false) &&
                            !this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(slotStack, 2, 38, false)) { // 输入槽位
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