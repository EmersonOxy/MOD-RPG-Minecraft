package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.client.InventoryMovement;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PauseScreen.class)
abstract class MovementPauseMixin extends Screen {
    private MovementPauseMixin(){super(Component.empty());}
    @Override public boolean isPauseScreen(){InventoryMovement.load();return !InventoryMovement.enabled;}
}
