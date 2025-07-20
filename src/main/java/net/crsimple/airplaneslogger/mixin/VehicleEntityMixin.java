package net.crsimple.airplaneslogger.mixin;

import immersive_aircraft.entity.VehicleEntity;
import net.crsimple.airplaneslogger.AirplanesLogger;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin {
    @Unique private String airPlanesLogger$owner;
    @Unique private Player airPlanesLogger$player;

    @Inject(method = "hurt",at = @At(value = "HEAD"))
    private void getPlayer(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(source.getEntity() instanceof Player player) {
            airPlanesLogger$player = player;
        }
    }

    @Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Limmersive_aircraft/entity/VehicleEntity;discard()V"))
    private void logPlaneBreakingCreative(VehicleEntity instance) {
        String ACTION = "убрал";
        Entity pilot = null;
        if(instance.getPassengers().isEmpty()) {
            if(airPlanesLogger$player != null) {
                pilot = airPlanesLogger$player;
            }
        } else {
            pilot = instance.getPassengers().getFirst();
        }

        if(pilot instanceof Player player) {
            AirplanesLogger.log(ACTION, instance.getEncodeId(), ((Entity) instance).getDisplayName(), player, airPlanesLogger$owner);
        }
        instance.discard();
    }
    @Redirect(method = "applyDamage", at = @At(value = "INVOKE", target = "Limmersive_aircraft/entity/VehicleEntity;discard()V"))
    private void logPlaneBreakingSurvival(VehicleEntity instance) {
        String ACTION = "убрал";


        Entity pilot = null;
        if(instance.getPassengers().isEmpty()) {
            if(airPlanesLogger$player != null) {
                pilot = airPlanesLogger$player;
            }
        } else {
            pilot = instance.getPassengers().getFirst();
        }

        if(pilot instanceof Player player) {
            AirplanesLogger.log(ACTION, instance.getEncodeId(), ((Entity) instance).getDisplayName(), player, airPlanesLogger$owner);
        }
        instance.discard();
    }
    @Redirect(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;startRiding(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean logEntitySitting(Player user, Entity entity) {
        if(entity == null) {
            return false;
        }

        String ACTION = "сел";
        var result = user.startRiding(entity);

        if(result) {
            AirplanesLogger.log(ACTION, entity.getEncodeId(), entity.getDisplayName(), user, airPlanesLogger$owner);
            return true;
        }
        return false;
    }

    @Inject(method = "readAdditionalSaveData",at = @At(value = "HEAD"))
    private void readOwnerTag(CompoundTag par1, CallbackInfo ci) {
        if(par1.contains("owner")) {
            airPlanesLogger$owner = par1.getString("owner");
        }
    }

    @Inject(method = "readItemTag", at = @At(value = "HEAD"))
    private void readOwnerTagToItem(ItemStack stack, CallbackInfo ci) {
        if (stack.get(DataComponents.CUSTOM_DATA) != null && stack.has(DataComponents.CUSTOM_DATA) && stack.get(DataComponents.CUSTOM_DATA).contains("owner")) {
            airPlanesLogger$owner = stack.get(DataComponents.CUSTOM_DATA).copyTag().getString("owner");
        }
    }
    @Inject(method = "addAdditionalSaveData",at = @At(value = "HEAD"))
    private void saveOwnerTag(CompoundTag par1, CallbackInfo ci) {
        if(airPlanesLogger$owner != null){
            par1.putString("owner", airPlanesLogger$owner);
        }
    }
    @Inject(method = "addItemTag",at = @At(value = "HEAD"))
    private void saveOwnerTagToItem(ItemStack stack, CallbackInfo ci) {
        if (airPlanesLogger$owner != null) {
            var tag = new CompoundTag();
            tag.putString("owner",airPlanesLogger$owner);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
