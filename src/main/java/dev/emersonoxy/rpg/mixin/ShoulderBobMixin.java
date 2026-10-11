package dev.emersonoxy.rpg.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emersonoxy.rpg.client.ShoulderCamera;
import dev.emersonoxy.rpg.client.TargetLock;
import dev.emersonoxy.rpg.client.CameraProjection;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=GameRenderer.class,remap=false)
public abstract class ShoulderBobMixin {
    @ModifyArg(method="renderLevel",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/PerspectiveProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"),index=0)
    private Matrix4f oxyrpg$projection(Matrix4f projection){CameraProjection.capture(projection);return projection;}
    // Positional sway replaces vanilla projection bob so it does not displace the crosshair.
    @Inject(method="bobView",at=@At("HEAD"),cancellable=true)
    private void oxyrpg$bob(PoseStack stack,float partial,CallbackInfo ci){if(ShoulderCamera.active()||TargetLock.mouseLocked())ci.cancel();}
}
