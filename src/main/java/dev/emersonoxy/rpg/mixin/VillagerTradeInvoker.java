package dev.emersonoxy.rpg.mixin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(value=Villager.class,remap=false)
public interface VillagerTradeInvoker {
    @Invoker("startTrading") void oxyStartTrading(Player player);
}
