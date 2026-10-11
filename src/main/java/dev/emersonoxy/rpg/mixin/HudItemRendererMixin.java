package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.client.HudOpacity;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value=GuiRenderer.class,remap=false)
public abstract class HudItemRendererMixin {
    @ModifyArgs(method="submitBlitFromItemAtlas",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/render/state/BlitRenderState;<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2f;IIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V"))
    private void oxyrpg$itemAlpha(Args args,GuiItemRenderState state,float x,float y,int itemSize,int atlasSize){args.set(11,HudOpacity.itemColor(state));}
}
