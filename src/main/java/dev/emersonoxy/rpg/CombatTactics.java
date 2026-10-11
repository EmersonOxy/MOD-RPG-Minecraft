package dev.emersonoxy.rpg;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Short repositioning windows; ordinary vanilla targeting and attacks resume afterwards. */
public final class CombatTactics {
    private CombatTactics() {}

    public static void join(EntityJoinLevelEvent event) {
        if(event.isCanceled()||!(event.getLevel() instanceof ServerLevel)
            ||!(event.getEntity() instanceof Mob mob)||RpgNecromancer.isSummon(mob)||RpgSpecialists.temporary(mob))return;
        // Preserve custom AI from other mods. Runtime goals are recreated for loaded entities.
        if(!net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getNamespace().equals("minecraft"))return;
        if(!(mob instanceof AbstractSkeleton||mob instanceof Zombie||mob instanceof Pillager||mob instanceof Vindicator))return;
        if(mob.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof Focus))
            mob.goalSelector.addGoal(1,new Focus(mob));
        if(mob.goalSelector.getAvailableGoals().stream().noneMatch(g->g.getGoal() instanceof Reposition))
            mob.goalSelector.addGoal(mob instanceof AbstractSkeleton?3:mob instanceof Pillager?2:1,new Reposition(mob));
    }

    /** Gives telegraphed casts and their short recovery a real interruption of ordinary attacks. */
    private static final class Focus extends Goal {
        private final Mob mob;
        Focus(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){
            if(!EliteMobs.elite(mob)||!mob.isAlive()||mob.isOnFire()||mob.isInWater()||mob.isInLava()||RpgNecromancer.isSummon(mob)||RpgSpecialists.temporary(mob))return false;
            var d=mob.getPersistentData();long now=mob.level().getGameTime();
            return d.getLongOr("oxy_elite_cast_until",0)>now||d.getLongOr("oxy_affix_cast",0)>now||d.getLongOr("oxy_elite_recover_until",0)>now;
        }
        @Override public void start(){mob.getNavigation().stop();}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){mob.getNavigation().stop();var target=mob.getTarget();if(target!=null)mob.getLookControl().setLookAt(target,30,30);}
    }

    private static final class Reposition extends Goal {
        private final Mob mob;
        private long next,until;
        private net.minecraft.world.level.pathfinder.Path path;
        private LivingEntity target;

        Reposition(Mob mob){this.mob=mob;next=mob.level().getGameTime()+40+mob.getRandom().nextInt(80);setFlags(EnumSet.of(Flag.MOVE));}
        private boolean eligible(LivingEntity e){
            return !RpgNecromancer.isSummon(mob)&&!RpgSpecialists.temporary(mob)&&e!=null&&e.isAlive()&&(e instanceof Player p?!p.isCreative()&&!p.isSpectator():RpgNecromancer.isSummon(e))
                &&!mob.isOnFire()&&!mob.isPassenger()&&!mob.isInWater()&&!mob.isInLava()&&mob.onGround()
                &&mob.getPersistentData().getLongOr("oxy_elite_cast_until",0)<=mob.level().getGameTime()
                &&mob.getPersistentData().getLongOr("oxy_affix_cast",0)<=mob.level().getGameTime();
        }
        @Override public boolean canUse(){
            long now=mob.level().getGameTime();if(now<next)return false;
            next=now+80+mob.getRandom().nextInt(40);target=mob.getTarget();
            if(!RpgConfig.tactics()||!eligible(target)||mob.isUsingItem()||!mob.hasLineOfSight(target))return false;
            double distance=mob.distanceToSqr(target);boolean ranged=mob.getMainHandItem().is(Items.BOW)||mob.getMainHandItem().is(Items.CROSSBOW);
            if(distance<(ranged?.25:16)||distance>(ranged?144:100))return false;
            // Retreat at close range; otherwise make a short lateral move. Melee flanks only one mob in three.
            if(!ranged&&Math.floorMod(mob.getUUID().hashCode(),3)!=0)return false;
            Vec3 away=mob.position().subtract(target.position()).multiply(1,0,1).normalize();
            double side=mob.getUUID().getLeastSignificantBits()%2==0?1:-1;
            Vec3 offset=ranged&&distance<36?away.scale(3):new Vec3(-away.z*side,0,away.x*side).scale(ranged?2.5:2);
            BlockPos pos=BlockPos.containing(mob.position().add(offset));
            if(!mob.level().hasChunkAt(pos)||!mob.level().getBlockState(pos.below()).isSolidRender()
                ||!mob.level().getBlockState(pos).canBeReplaced()||!mob.level().getBlockState(pos).getFluidState().isEmpty())return false;
            path=mob.getNavigation().createPath(pos,0);return path!=null&&path.canReach();
        }
        @Override public void start(){until=mob.level().getGameTime()+16;mob.getNavigation().moveTo(path,1.05);}
        @Override public boolean canContinueToUse(){return RpgConfig.tactics()&&mob.getTarget()==target&&eligible(target)
            &&mob.level().getGameTime()<until&&!mob.getNavigation().isDone();}
        @Override public void stop(){mob.getNavigation().stop();path=null;target=null;}
    }
}
