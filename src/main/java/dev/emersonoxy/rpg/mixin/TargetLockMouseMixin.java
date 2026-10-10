package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.client.TargetLock;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=MouseHandler.class,remap=false)
public abstract class TargetLockMouseMixin {
    @Inject(method="turnPlayer",at=@At("HEAD"),cancellable=true)
    private void oxyrpg$lockedMouse(double time,CallbackInfo ci){if(TargetLock.mouseLocked())ci.cancel();}
}
