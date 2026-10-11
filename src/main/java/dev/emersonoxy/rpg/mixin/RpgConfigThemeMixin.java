package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.client.RpgUi;
import org.spongepowered.asm.mixin.*;
@Mixin(net.neoforged.neoforge.client.gui.ConfigurationScreen.class)
abstract class RpgConfigThemeMixin implements RpgUi.ConfigTheme {
    @Shadow @Final protected net.neoforged.fml.ModContainer mod;
    @Override public boolean oxyOwned(){return mod.getModId().equals("oxyrpg");}
}
