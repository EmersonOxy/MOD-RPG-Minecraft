package dev.emersonoxy.rpg.mixin;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(targets="net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$MoreTab")
abstract class TerralithWorldTabMixin extends GridLayoutTab {
    @Shadow @Final private CreateWorldScreen this$0;
    private TerralithWorldTabMixin(){super(Component.empty());}
    @Inject(method="<init>(Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;)V",at=@At("RETURN"))
    private void oxyTerralith(CreateWorldScreen screen,CallbackInfo ci){layout.addChild(Button.builder(Component.literal(dev.emersonoxy.rpg.client.TerralithChoice.label(screen)),b->dev.emersonoxy.rpg.client.TerralithChoice.toggle(screen)).width(210).build(),3,0);}
}
