package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.AviaryCarry;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class AviaryCarryMixin {
    @Inject(method="getPassengerRidingPosition(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/Vec3;",at=@At("HEAD"),cancellable=true)
    private void oxyCarryPosition(Entity passenger,CallbackInfoReturnable<Vec3> ci){
        if((Object)this instanceof Player p&&AviaryCarry.holding(p,passenger))ci.setReturnValue(p.position().add(0,-passenger.getBbHeight()-.2,0));
    }
}
