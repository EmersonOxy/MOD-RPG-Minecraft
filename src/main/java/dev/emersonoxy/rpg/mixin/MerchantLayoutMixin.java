package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.client.VillagerClient;
import net.minecraft.client.gui.screens.inventory.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractContainerScreen.class)
abstract class MerchantLayoutMixin {
    @Shadow protected int leftPos;
    @Inject(method="init()V",at=@At("TAIL"))
    private void oxyLayout(CallbackInfo ci){if((Object)this instanceof MerchantScreen screen&&VillagerClient.data()!=null){int side=VillagerClient.sideWidth(screen.width);if(side>0)leftPos=(screen.width-288-side)/2+6;}}
}
