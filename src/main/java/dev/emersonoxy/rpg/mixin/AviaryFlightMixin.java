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
    @Inject(method="travel",at=@At("HEAD"))
    private void oxyFeatherStart(net.minecraft.world.phys.Vec3 movement,CallbackInfo ci){
        if((Object)this instanceof net.minecraft.world.entity.player.Player p&&RpgAviary.featherFalling(p)){if(p.isFallFlying())p.stopFallFlying();RpgAviary.featherMovement(p);}
    }
    @Inject(method="travel",at=@At("RETURN"))
    private void oxyFeatherEnd(net.minecraft.world.phys.Vec3 movement,CallbackInfo ci){
        if((Object)this instanceof net.minecraft.world.entity.player.Player p&&RpgAviary.featherFalling(p))RpgAviary.featherMovement(p);
    }
    // Vanilla's maintenance selects an equipped glider to damage; our flight has no such item.
    @Inject(method="updateFallFlying",at=@At("HEAD"),cancellable=true)
    private void oxyMaintenance(CallbackInfo ci){if(RpgAviary.canFly((LivingEntity)(Object)this))ci.cancel();}
}
