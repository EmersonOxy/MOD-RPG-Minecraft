package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.AbilityAim;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Projectile.class)
abstract class NativeProjectileAimMixin {
    @Inject(method="shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V",at=@At("HEAD"),cancellable=true)
    private void oxyShot(Entity shooter,float pitch,float yaw,float offset,float speed,float spread,CallbackInfo ci){if(shooter instanceof ServerPlayer p&&AbilityAim.nativeShot((Projectile)(Object)this,p,speed,spread,net.minecraft.util.Mth.wrapDegrees(yaw-p.getYRot())))ci.cancel();}
}
