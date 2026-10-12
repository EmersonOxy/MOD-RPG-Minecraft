package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.client.CameraAim;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class CameraPickMixin {
    @Inject(method="pick(F)V",at=@At("TAIL"))
    private void oxyPick(float partial,CallbackInfo ci){CameraAim.pick(partial);}
}
