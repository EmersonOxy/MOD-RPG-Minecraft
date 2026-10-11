package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.client.RpgUi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractSliderButton.class)
abstract class RpgSliderThemeMixin {
    @Shadow protected double value;
    @Inject(method="renderWidget",at=@At("HEAD"),cancellable=true)
    private void oxySlider(GuiGraphics graphics,int mouseX,int mouseY,float partial,CallbackInfo ci){if(RpgUi.owns(Minecraft.getInstance().screen)){RpgUi.slider(graphics,(AbstractSliderButton)(Object)this,value);ci.cancel();}}
}
