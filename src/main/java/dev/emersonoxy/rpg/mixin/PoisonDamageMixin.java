package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.ConditionDamage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets="net.minecraft.world.effect.PoisonMobEffect")
abstract class PoisonDamageMixin {
    @Inject(method="applyEffectTick",at=@At("HEAD")) private void oxyPoison(ServerLevel level,LivingEntity entity,int amp,CallbackInfoReturnable<Boolean> ci){ConditionDamage.enter(0x8ED45D);}
    @Inject(method="applyEffectTick",at=@At("RETURN")) private void oxyEnd(ServerLevel level,LivingEntity entity,int amp,CallbackInfoReturnable<Boolean> ci){ConditionDamage.leave();}
}
