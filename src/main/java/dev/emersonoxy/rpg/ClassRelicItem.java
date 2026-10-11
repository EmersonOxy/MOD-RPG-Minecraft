package dev.emersonoxy.rpg;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
/** A registered class artifact activates through the game's configurable Use Item binding. */
public class ClassRelicItem extends Item {
    public ClassRelicItem(Properties properties){super(properties);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){if(player instanceof ServerPlayer p)ClassRelics.use(p,player.getItemInHand(hand));return InteractionResult.SUCCESS;}
    @Override public InteractionResult useOn(UseOnContext context){return use(context.getLevel(),context.getPlayer(),context.getHand());}
    public static final class HunterBow extends BowItem {
        public HunterBow(Properties properties){super(properties);}
        @Override public InteractionResult use(Level level,Player player,InteractionHand hand){var stack=player.getItemInHand(hand);
            if(!player.hasInfiniteMaterials()&&player.getProjectile(stack).isEmpty()){
                if(player instanceof ServerPlayer p)Professions.notify(p,"Você precisa de flechas para usar o arco.");
                return InteractionResult.FAIL;
            }
            var result=super.use(level,player,hand);
            if(result.consumesAction()&&player instanceof ServerPlayer p)ClassRelics.use(p,stack);
            return result;}
    }
}
