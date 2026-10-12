package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.AbilityAim;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CrossbowItem.class)
abstract class NativeCrossbowAimMixin {
    @Inject(method="shootProjectile(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/projectile/Projectile;IFFFLnet/minecraft/world/entity/LivingEntity;)V",at=@At("HEAD"),cancellable=true)
    private void oxyShot(LivingEntity shooter,Projectile shot,int index,float speed,float spread,float angle,LivingEntity target,CallbackInfo ci){if(shooter instanceof ServerPlayer p&&AbilityAim.nativeShot(shot,p,speed,spread,angle))ci.cancel();}
}
