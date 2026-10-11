package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.client.HudOpacity;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=GuiGraphics.class,remap=false)
public abstract class HudGraphicsMixin {
    @ModifyVariable(method="submitBlit",at=@At("HEAD"),argsOnly=true,ordinal=4)
    private int oxyrpg$blitAlpha(int color){return HudOpacity.color(color);}
    @ModifyVariable(method="submitTiledBlit",at=@At("HEAD"),argsOnly=true,ordinal=6)
    private int oxyrpg$tiledAlpha(int color){return HudOpacity.color(color);}
    @ModifyVariable(method="submitColoredRectangle",at=@At("HEAD"),argsOnly=true,ordinal=4)
    private int oxyrpg$rectangleAlpha(int color){return HudOpacity.color(color);}
    @ModifyVariable(method="submitColoredRectangle",at=@At("HEAD"),argsOnly=true,ordinal=0)
    private Integer oxyrpg$gradientAlpha(Integer color){return HudOpacity.color(color);}
    @ModifyVariable(method="drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V",at=@At("HEAD"),argsOnly=true,ordinal=2)
    private int oxyrpg$textAlpha(int color){return HudOpacity.color(color);}
}
