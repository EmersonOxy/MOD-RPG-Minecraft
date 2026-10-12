package dev.emersonoxy.rpg;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
/** A separate, persistent villager-bodied guard; existing residents are never converted. */
public final class VillageGuard extends Villager {
    private int strike;
    public VillageGuard(EntityType<? extends Villager> type,Level level){super(type,level);setPersistenceRequired();setCustomName(net.minecraft.network.chat.Component.literal("Guarda da aldeia"));setCustomNameVisible(true);setItemSlot(EquipmentSlot.MAINHAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD));}
    @Override protected void customServerAiStep(ServerLevel level){
        var data=getPersistentData();if(!data.contains("oxy_guard_x")){data.putDouble("oxy_guard_x",getX());data.putDouble("oxy_guard_y",getY());data.putDouble("oxy_guard_z",getZ());}
        if(VillagerQuests.conversing(this)){getNavigation().stop();setDeltaMovement(0,getDeltaMovement().y,0);return;}
        if(tickCount%10==0){var enemies=level.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(16),m->VampireHunt.threat(this,m));
            var tag=getPersistentData();var home=new net.minecraft.world.phys.Vec3(tag.getDoubleOr("oxy_guard_x",getX()),tag.getDoubleOr("oxy_guard_y",getY()),tag.getDoubleOr("oxy_guard_z",getZ()));
            var target=enemies.stream().filter(m->m.position().distanceToSqr(home)<32*32).filter(this::hasLineOfSight).min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);setTarget(target);
            if(target!=null)getNavigation().moveTo(target,.85);
            else if(position().distanceToSqr(home)>144)getNavigation().moveTo(home.x,home.y,home.z,.7);
            else if(tickCount%80==0){var to=net.minecraft.world.entity.ai.util.DefaultRandomPos.getPos(this,8,3);if(to!=null)getNavigation().moveTo(to.x,to.y,to.z,.6);}
        }
        var target=getTarget();if(target!=null&&target.isAlive()){getLookControl().setLookAt(target,30,30);if(strike--<=0&&distanceToSqr(target)<4&&hasLineOfSight(target)){strike=20;swing(net.minecraft.world.InteractionHand.MAIN_HAND);target.hurtServer(level,damageSources().mobAttack(this),5);}}
        if(getHealth()<getMaxHealth()&&tickCount%200==0)heal(1);
    }
}
