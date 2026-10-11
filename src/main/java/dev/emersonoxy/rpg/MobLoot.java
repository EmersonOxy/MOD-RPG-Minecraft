package dev.emersonoxy.rpg;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** Level gates quality; luck increases the roll chance without bypassing those gates. */
public final class MobLoot {
    private MobLoot(){}
    public static void reward(ServerPlayer player,LivingEntity entity,int luck){
        if(!(entity instanceof Mob mob)||!(mob instanceof Enemy)||RpgNecromancer.isSummon(mob)||mob.getPersistentData().getBooleanOr("oxy_elite_child",false)||!player.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))return;
        int level=MobLevels.level(mob);boolean elite=EliteMobs.elite(mob);int rolls=elite?2:1;
        for(int i=0;i<rolls;i++)if(elite||mob.getRandom().nextFloat()<Math.min(.65,.10+level*.002+luck*.002)){
            int roll=mob.getRandom().nextInt(100);Item item;
            if(level>=85&&roll>=97)item=Items.NETHERITE_SCRAP;
            else if(level>=60&&roll>=85)item=Items.DIAMOND;
            else if(level>=40&&roll>=65)item=Items.EMERALD;
            else if(level>=20&&roll>=45)item=Items.GOLD_INGOT;
            else item=level<20?Items.COPPER_INGOT:Items.IRON_INGOT;
            int count=item==Items.DIAMOND||item==Items.NETHERITE_SCRAP?1:1+level/35+(elite?1:0);
            drop(mob,new ItemStack(item,count));
        }
        if(elite)drop(mob,new ItemStack(level<40?Items.IRON_INGOT:Items.EMERALD,2+level/25));
    }
    public static void drops(LivingDropsEvent event){
        if(event.isCanceled()||!(event.getEntity() instanceof Mob mob)||!(mob instanceof Enemy)||RpgNecromancer.isSummon(mob)||mob.getPersistentData().getBooleanOr("oxy_elite_child",false)||RpgNecromancer.owner(event.getSource())==null)return;
        int extra=MobLevels.level(mob)/40+(EliteMobs.elite(mob)?1:0);if(extra==0)return;
        for(var drop:event.getDrops()){var stack=drop.getItem();if(stack.getMaxStackSize()>1&&!stack.has(DataComponents.CUSTOM_NAME)&&!stack.has(DataComponents.CUSTOM_DATA))stack.setCount(Math.min(stack.getMaxStackSize(),stack.getCount()+Math.min(4,extra)));}
    }
    private static void drop(Mob mob,ItemStack stack){mob.level().addFreshEntity(new ItemEntity(mob.level(),mob.getX(),mob.getY()+.3,mob.getZ(),stack));}
}
