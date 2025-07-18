package net.crsimple.airplaneslogger.mixin;

import immersive_aircraft.item.VehicleItem;
import net.crsimple.airplaneslogger.AirplanesLogger;
import net.crsimple.airplaneslogger.util.OwnerItemData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VehicleItem.class)
public class VehicleItemMixin implements OwnerItemData {
    @Inject(method = "use",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",shift = At.Shift.AFTER))
    private void logPlacingPlane(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        String ACTION = "поставил";
        var plane = user.getItemInHand(hand);

        if(plane.getItem() != Items.AIR) {
            AirplanesLogger.log(ACTION,plane.getDescriptionId(), plane.getDisplayName(),user);
        }
    }

    @Override
    public String getOwner(ItemStack stack) {
        if(stack.has(DataComponents.CUSTOM_DATA) && stack.get(DataComponents.CUSTOM_DATA).contains("owner")) {
            return stack.get(DataComponents.CUSTOM_DATA).copyTag().getString("owner");
        }
        return null;
    }

    @Override
    public void setOwner(ItemStack stack, String owner) {
        if (owner != null) {
            var tag = new CompoundTag();
            tag.putString("owner",owner);
            stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        }
    }
}
