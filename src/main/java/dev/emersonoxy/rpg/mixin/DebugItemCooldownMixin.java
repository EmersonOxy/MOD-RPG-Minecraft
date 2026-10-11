package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.RpgDebug;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only Oxy consumables bypass the native timer; vanilla and other mods keep their cooldowns. */
@Mixin(value=UseCooldown.class,remap=false)
abstract class DebugItemCooldownMixin {
    @Inject(method="apply",at=@At("HEAD"),cancellable=true)
    private void oxyDebugCooldown(ItemStack stack,LivingEntity entity,CallbackInfo ci){
        if(entity instanceof Player p&&RpgDebug.cooldownsDisabled(p)
            &&net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("oxyrpg"))ci.cancel();
    }
}
