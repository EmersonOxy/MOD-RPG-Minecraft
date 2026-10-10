package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.EquipmentAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets="net.minecraft.world.inventory.ArmorSlot",remap=false)
public abstract class ArmorSlotMixin {
    @Shadow @Final private LivingEntity owner;
    @Inject(method="mayPlace",at=@At("HEAD"),cancellable=true,remap=false)
    private void oxyrpg$allowArmor(ItemStack stack,CallbackInfoReturnable<Boolean> callback){
        if(owner instanceof Player player&&!EquipmentAccess.allowed(player,stack))callback.setReturnValue(false);
    }
}
