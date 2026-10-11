package dev.emersonoxy.rpg.mixin;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.phys.Vec2;
import dev.emersonoxy.rpg.client.InventoryMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=KeyboardInput.class,remap=false)
public abstract class InventoryMovementMixin extends ClientInput {
    @Inject(method="tick",at=@At("RETURN"))
    private void oxyInventory(CallbackInfo ci){var input=InventoryMovement.input();if(input==null)return;this.keyPresses=input;this.moveVector=new Vec2((input.left()?1:0)-(input.right()?1:0),(input.forward()?1:0)-(input.backward()?1:0)).normalized();}
}
