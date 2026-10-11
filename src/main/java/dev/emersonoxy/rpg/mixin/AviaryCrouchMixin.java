package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.RpgAviary;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** LocalPlayer has a separate cached crouch flag; suppress it only for open-air Featherweight. */
@Mixin(value=LocalPlayer.class,remap=false)
abstract class AviaryCrouchMixin {
    @Inject(method="isCrouching",at=@At("HEAD"),cancellable=true)
    private void oxyAirCrouch(CallbackInfoReturnable<Boolean> ci){
        var p=(LocalPlayer)(Object)this;
        if(RpgAviary.uprightAir(p)&&(p.isFallFlying()||p.level().noCollision(p,p.getDimensions(Pose.STANDING).makeBoundingBox(p.position()).deflate(1.0E-7))))ci.setReturnValue(false);
    }
}
