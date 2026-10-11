package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.RpgAviary;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Preserve sneak input for braking, but choose an upright pose where there is room. */
@Mixin(Player.class)
abstract class AviaryPoseMixin {
    @Shadow protected abstract boolean canPlayerFitWithinBlocksAndEntitiesWhen(Pose pose);
    @Inject(method="getDesiredPose",at=@At("HEAD"),cancellable=true)
    private void oxyAirPose(CallbackInfoReturnable<Pose> ci){
        var p=(Player)(Object)this;
        if(RpgAviary.uprightAir(p)&&!p.isFallFlying()&&!p.isSwimming()&&!p.isAutoSpinAttack()&&canPlayerFitWithinBlocksAndEntitiesWhen(Pose.STANDING))ci.setReturnValue(Pose.STANDING);
    }
}
