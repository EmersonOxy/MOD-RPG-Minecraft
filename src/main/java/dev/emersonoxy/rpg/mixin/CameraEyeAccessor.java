package dev.emersonoxy.rpg.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraEyeAccessor {
    @Accessor("eyeHeight") float oxyEyeHeight();
    @Accessor("eyeHeightOld") float oxyOldEyeHeight();
}
