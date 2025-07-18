package net.crsimple.airplaneslogger.util;

import net.minecraft.world.item.ItemStack;

public interface OwnerItemData {
    String getOwner(ItemStack stack);
    void setOwner(ItemStack stack, String owner);
}
