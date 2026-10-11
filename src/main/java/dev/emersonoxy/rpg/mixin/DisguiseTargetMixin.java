package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.RpgSpecialists;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Covers target acquisition as well as goals that validate targets without a change event. */
@Mixin(LivingEntity.class)
abstract class DisguiseTargetMixin {
    @Inject(method="canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z",at=@At("HEAD"),cancellable=true)
    private void oxyDisguise(LivingEntity target,CallbackInfoReturnable<Boolean> ci){if((Object)this instanceof Mob&&target instanceof Player p&&(RpgSpecialists.intangible(p)||RpgSpecialists.disguised(p)))ci.setReturnValue(false);}
}
