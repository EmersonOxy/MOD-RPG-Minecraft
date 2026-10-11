package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.RpgAviary;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class AviaryFlightMixin {
    @Inject(method="canGlide",at=@At("HEAD"),cancellable=true)
    private void oxyGlide(CallbackInfoReturnable<Boolean> ci){if(RpgAviary.canFly((LivingEntity)(Object)this))ci.setReturnValue(true);}
    // Vanilla's maintenance selects an equipped glider to damage; our flight has no such item.
    @Inject(method="updateFallFlying",at=@At("HEAD"),cancellable=true)
    private void oxyMaintenance(CallbackInfo ci){if(RpgAviary.canFly((LivingEntity)(Object)this))ci.cancel();}
}
