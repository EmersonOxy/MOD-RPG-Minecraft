package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.client.RpgUi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractButton.class)
abstract class RpgButtonThemeMixin {
    @Inject(method="renderWidget",at=@At("HEAD"),cancellable=true)
    private void oxyButton(GuiGraphics graphics,int mouseX,int mouseY,float partial,CallbackInfo ci){var screen=Minecraft.getInstance().screen;if(RpgUi.owns(screen)){RpgUi.action(graphics,(AbstractButton)(Object)this);ci.cancel();}}
}
