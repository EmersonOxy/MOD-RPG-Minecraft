package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MerchantScreen.class)
abstract class MerchantThemeMixin {
    @Shadow private int shopItem;
    @Inject(method="renderBg",at=@At("HEAD"),cancellable=true)
    private void oxyBackground(GuiGraphics g,float delta,int mx,int my,CallbackInfo ci){MerchantTheme.background(g,(MerchantScreen)(Object)this,shopItem);ci.cancel();}
    @Inject(method="renderLabels",at=@At("HEAD"),cancellable=true)
    private void oxyLabels(GuiGraphics g,int mx,int my,CallbackInfo ci){var font=Minecraft.getInstance().font;var screen=(MerchantScreen)(Object)this;g.drawCenteredString(font,"TROCAS",49,6,RpgUi.GOLD);g.drawCenteredString(font,font.plainSubstrByWidth(screen.getTitle().getString(),158),187,6,RpgUi.GOLD);g.drawString(font,"Inventário",107,73,RpgUi.MUTED);ci.cancel();}
}
