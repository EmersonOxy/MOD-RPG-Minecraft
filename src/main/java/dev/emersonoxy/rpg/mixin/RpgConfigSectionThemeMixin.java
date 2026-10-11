package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.client.RpgUi;
import org.spongepowered.asm.mixin.*;
@Mixin(net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen.class)
abstract class RpgConfigSectionThemeMixin implements RpgUi.ConfigTheme {
    @Shadow @Final protected net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen.Context context;
    @Override public boolean oxyOwned(){return context.modId().equals("oxyrpg");}
}
