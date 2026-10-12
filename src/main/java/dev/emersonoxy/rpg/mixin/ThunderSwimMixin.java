package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.AdvancedClasses;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Player.class)
abstract class ThunderSwimMixin {
    @Inject(method="updateSwimming()V",at=@At("HEAD"),cancellable=true)
    private void oxyNoSwimming(CallbackInfo ci){var p=(Player)(Object)this;if(AdvancedClasses.cannotSwim(p)){p.setSwimming(false);ci.cancel();}}
}
