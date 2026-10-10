package dev.emersonoxy.rpg.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emersonoxy.rpg.client.ShoulderCamera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=GameRenderer.class,remap=false)
public abstract class ShoulderBobMixin {
    // Positional sway replaces vanilla projection bob so it does not displace the crosshair.
    @Inject(method="bobView",at=@At("HEAD"),cancellable=true)
    private void oxyrpg$bob(PoseStack stack,float partial,CallbackInfo ci){if(ShoulderCamera.active())ci.cancel();}
}
