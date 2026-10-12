package dev.emersonoxy.rpg.mixin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class)
abstract class CompanionPushMixin {
    @Inject(method="push(Lnet/minecraft/world/entity/Entity;)V",at=@At("HEAD"),cancellable=true)
    private void oxyCompanion(Entity other,CallbackInfo ci){
        var self=(Entity)(Object)this;
        if(self instanceof Player&&visual(other)||other instanceof Player&&visual(self))ci.cancel();
    }
    private static boolean visual(Entity e){return e instanceof net.minecraft.world.entity.ambient.Bat||e.getPersistentData().getBooleanOr("oxy_cosmetic",false);}
}
