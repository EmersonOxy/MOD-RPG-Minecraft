package dev.emersonoxy.rpg.mixin;

import dev.emersonoxy.rpg.EquipmentAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=ServerGamePacketListenerImpl.class,remap=false)
public abstract class CreativeEquipmentMixin {
    @Shadow public ServerPlayer player;
    @Inject(method="handleSetCreativeModeSlot",at=@At("HEAD"),cancellable=true,remap=false)
    private void oxyrpg$allowCreativeEquipment(ServerboundSetCreativeModeSlotPacket packet,CallbackInfo callback){
        // Vanilla schedules network-thread calls onto the server thread; guard the scheduled invocation.
        if(!player.level().getServer().isSameThread())return;
        int slot=packet.slotNum();
        if((slot>=5&&slot<=8||slot==45)&&!EquipmentAccess.allowed(player,packet.itemStack())){
            player.inventoryMenu.sendAllDataToRemote();callback.cancel();
        }
    }
}
