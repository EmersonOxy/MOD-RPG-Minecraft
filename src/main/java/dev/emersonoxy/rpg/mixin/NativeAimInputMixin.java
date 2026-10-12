package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.client.LiveAim;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(MultiPlayerGameMode.class)
abstract class NativeAimInputMixin {
    @Inject(method="releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V",at=@At("HEAD"))
    private void oxyRelease(Player p,CallbackInfo ci){LiveAim.send();}
    @Inject(method="useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",at=@At("HEAD"))
    private void oxyUse(Player p,InteractionHand hand,CallbackInfoReturnable<InteractionResult> ci){LiveAim.send();}
}
