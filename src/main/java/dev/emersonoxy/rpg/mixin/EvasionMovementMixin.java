package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.RpgEvasion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(Entity.class)
abstract class EvasionMovementMixin {
    @ModifyVariable(method="move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private Vec3 oxyLimitRoll(Vec3 movement){return (Object)this instanceof Player p?RpgEvasion.movement(p,movement):movement;}
}
