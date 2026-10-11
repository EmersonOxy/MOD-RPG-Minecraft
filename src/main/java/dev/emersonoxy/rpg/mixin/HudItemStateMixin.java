package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.client.HudOpacity;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=GuiItemRenderState.class,remap=false)
public abstract class HudItemStateMixin {
    @Inject(method="<init>",at=@At("TAIL"))
    private void oxyrpg$captureAlpha(CallbackInfo ci){HudOpacity.capture((GuiItemRenderState)(Object)this);}
}
