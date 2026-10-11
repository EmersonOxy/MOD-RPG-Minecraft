package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.RpgVampire;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(targets="net.minecraft.world.effect.HealOrHarmMobEffect")
abstract class VampirePotionMixin {
    @Inject(method="applyEffectTick",at=@At("HEAD"),cancellable=true) private void oxyDrink(ServerLevel level,LivingEntity entity,int amp,CallbackInfoReturnable<Boolean> ci){if(entity instanceof Player p&&RpgVampire.twilight(p))ci.setReturnValue(true);}
    @Inject(method="applyInstantenousEffect",at=@At("HEAD"),cancellable=true) private void oxySplash(ServerLevel level,Entity source,Entity owner,LivingEntity entity,int amp,double amount,CallbackInfo ci){if(entity instanceof Player p&&RpgVampire.twilight(p))ci.cancel();}
}
