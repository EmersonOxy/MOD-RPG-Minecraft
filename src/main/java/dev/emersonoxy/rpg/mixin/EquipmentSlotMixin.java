package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.EquipmentAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=Slot.class,remap=false)
public abstract class EquipmentSlotMixin {
    @Inject(method="mayPlace",at=@At("HEAD"),cancellable=true,remap=false)
    private void oxyrpg$allowOffhand(ItemStack stack,CallbackInfoReturnable<Boolean> callback){
        var slot=(Slot)(Object)this;
        if(slot.container instanceof Inventory inventory&&slot.getContainerSlot()==40&&!EquipmentAccess.allowed(inventory.player,stack))callback.setReturnValue(false);
    }
}
