package com.talhanation.recruits.corelib;

import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class ContainerBase extends AbstractContainerMenu {
   @Nullable
   protected Container inventory;
   protected Container playerInventory;

   public ContainerBase(MenuType containerType, int id, Container playerInventory, Container inventory) {
      super(containerType, id);
      this.playerInventory = playerInventory;
      this.inventory = inventory;
   }

   protected void addPlayerInventorySlots() {
      if (this.playerInventory != null) {
         for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
               this.addSlot(new Slot(this.playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + this.getInvOffset()));
            }
         }

         for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(this.playerInventory, k, 8 + k * 18, 142 + this.getInvOffset()));
         }
      }
   }

   public int getInvOffset() {
      return 0;
   }

   public int getInventorySize() {
      return this.inventory == null ? 0 : this.inventory.getContainerSize();
   }

   @Nullable
   public Container getPlayerInventory() {
      return this.playerInventory;
   }

   public ItemStack quickMoveStack(Player playerIn, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack stack = slot.getItem();
         itemstack = stack.copy();
         if (index < this.getInventorySize()) {
            if (!this.moveItemStackTo(stack, this.getInventorySize(), this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(stack, 0, this.getInventorySize(), false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }

      return itemstack;
   }

   public boolean stillValid(Player player) {
      return this.inventory == null ? true : this.inventory.stillValid(player);
   }

   public void removed(Player player) {
      super.removed(player);
      if (this.inventory != null) {
         this.inventory.stopOpen(player);
      }
   }
}
