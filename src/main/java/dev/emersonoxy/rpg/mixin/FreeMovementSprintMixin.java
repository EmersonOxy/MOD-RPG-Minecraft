package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.client.MovementFacing;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.ClientInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=LocalPlayer.class,remap=false)
abstract class FreeMovementSprintMixin {
    @Redirect(method={"aiStep","canStartSprinting","shouldStopRunSprinting"},at=@At(value="INVOKE",target="Lnet/minecraft/client/player/ClientInput;hasForwardImpulse()Z"))
    private boolean oxySprintDirection(ClientInput input){return MovementFacing.sprintImpulse(input);}
}
