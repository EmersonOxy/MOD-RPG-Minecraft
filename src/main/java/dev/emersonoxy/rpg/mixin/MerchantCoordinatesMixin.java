package dev.emersonoxy.rpg.mixin;

import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Native offer drawing/buttons/scroller must use the same origin as inventory slots. */
@Mixin(MerchantScreen.class)
abstract class MerchantCoordinatesMixin {
    private int oxyOrigin(int original) { return ((MerchantScreen)(Object)this).getGuiLeft(); }
    @ModifyVariable(method="init()V", at=@At("STORE"), index=1)
    private int oxyButtons(int original) { return oxyOrigin(original); }
    @ModifyVariable(method="renderContents(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at=@At("STORE"), index=6)
    private int oxyOffers(int original) { return oxyOrigin(original); }
    @ModifyVariable(method="mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z", at=@At("STORE"), index=3)
    private int oxyScroller(int original) { return oxyOrigin(original); }
}
