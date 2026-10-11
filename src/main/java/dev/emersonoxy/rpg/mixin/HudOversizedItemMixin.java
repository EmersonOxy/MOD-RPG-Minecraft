package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.client.HudOpacity;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.OversizedItemRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value=PictureInPictureRenderer.class,remap=false)
public abstract class HudOversizedItemMixin {
    @ModifyArgs(method="blitTexture",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/render/state/BlitRenderState;<init>(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/client/gui/render/TextureSetup;Lorg/joml/Matrix3x2f;IIIIFFFFILnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)V"))
    private void oxyrpg$itemAlpha(Args args,PictureInPictureRenderState state,GuiRenderState gui){
        if(state instanceof OversizedItemRenderState item)args.set(11,HudOpacity.itemColor(item.guiItemRenderState()));
    }
}
