package dev.emersonoxy.rpg;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** Vanilla drinking consumes one bottle; the server restores the real mana pool. */
public final class ManaPotionItem extends Item {
    private final int restored;
    public ManaPotionItem(Properties properties,int restored){super(properties);this.restored=restored;}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        if(player instanceof ServerPlayer p&&!RpgResources.manaMissing(p)){
            p.displayClientMessage(Component.translatable("message.oxyrpg.mana_full"),true);
            return InteractionResult.FAIL;
        }
        return super.use(level,player,hand);
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity){
        if(entity instanceof ServerPlayer p)RpgResources.restoreMana(p,restored);
        return super.finishUsingItem(stack,level,entity);
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> lines,TooltipFlag flag){
        lines.accept(Component.translatable("tooltip.oxyrpg.mana_potion",restored).withStyle(ChatFormatting.AQUA));
        lines.accept(Component.translatable("tooltip.oxyrpg.mana_potion_limit").withStyle(ChatFormatting.GRAY));
    }
}
