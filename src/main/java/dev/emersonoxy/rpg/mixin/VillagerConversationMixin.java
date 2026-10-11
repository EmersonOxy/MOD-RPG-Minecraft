package dev.emersonoxy.rpg.mixin;
import dev.emersonoxy.rpg.VillagerQuests;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Villager.class,remap=false)
public abstract class VillagerConversationMixin {
    @Inject(method="customServerAiStep",at=@At("HEAD"),cancellable=true)
    private void oxyConversation(ServerLevel level,CallbackInfo ci){var v=(Villager)(Object)this;if(!VillagerQuests.conversing(v))return;v.getNavigation().stop();v.getMoveControl().setWantedPosition(v.getX(),v.getY(),v.getZ(),0);v.setDeltaMovement(0,v.getDeltaMovement().y,0);ci.cancel();}
}
