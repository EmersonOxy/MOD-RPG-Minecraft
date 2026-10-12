package dev.emersonoxy.rpg;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Existing AI remains in charge; fear supplies navigation intent, never immobilization. */
public final class VampireHunt {
    public static final ResourceKey<DamageType> BITE=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("oxyrpg","vampire_bite"));
    private static final ResourceLocation FEAR=ResourceLocation.fromNamespaceAndPath("oxyrpg","vampire_fear_speed");
    private VampireHunt(){}
    public static boolean night(ServerLevel w){return w.dimensionType().hasSkyLight()&&Math.floorMod(w.getDayTime(),24000)>=12000;}
    public static boolean afraid(Villager v){return v.getPersistentData().getLongOr("oxy_vampire_fear",0)>v.level().getGameTime();}
    public static void threaten(LivingEntity target,ServerPlayer vampire){
        if(!(target instanceof Mob m)||!m.isAlive())return;
        if(m instanceof Enemy){m.setTarget(vampire);m.setLastHurtByMob(vampire);return;}
        var away=m.position().subtract(vampire.position()).multiply(1,0,1);
        if(away.lengthSqr()<.01)away=new net.minecraft.world.phys.Vec3(1,0,0);away=away.normalize();
        var to=m.position().add(away.scale(5));if(m instanceof PathfinderMob walker){var safe=net.minecraft.world.entity.ai.util.DefaultRandomPos.getPosAway(walker,8,3,vampire.position());if(safe!=null)to=safe;}
        if(m instanceof Villager v){
            var speed=v.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);if(speed!=null&&!speed.hasModifier(FEAR))speed.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(FEAR,-.3,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            if(v.isSleeping())v.stopSleeping();v.getPersistentData().putLong("oxy_vampire_fear",v.level().getGameTime()+60);
            if(v.getTradingPlayer()!=null){var trader=v.getTradingPlayer();v.setTradingPlayer(null);trader.closeContainer();}
            VillagerQuests.frighten(v);
            v.getBrain().setMemory(MemoryModuleType.NEAREST_HOSTILE,vampire);
            v.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            v.getBrain().setMemory(MemoryModuleType.WALK_TARGET,new WalkTarget(new BlockPosTracker(BlockPos.containing(to)),0.65f,1));
        }
        m.getNavigation().moveTo(to.x,to.y,to.z,.65);
    }
    public static void tick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof Mob m)||!(m.level() instanceof ServerLevel w)||!m.isAlive()||m.tickCount%10!=0||RpgVampire.proxy(m)||RpgNecromancer.isSummon(m)||RpgSpecialists.temporary(m))return;
        if(m instanceof Villager v&&!afraid(v)){var speed=v.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);if(speed!=null)speed.removeModifier(FEAR);}
        boolean wounded=m.hasEffect(Bleeding.EFFECT);
        if(wounded&&Bleeding.owner(m) instanceof ServerPlayer vampire&&vampire.isAlive()&&vampire.level()==w&&RpgAbilities.profile(vampire).rpgClass()==RpgClass.VAMPIRESCO&&m.distanceToSqr(vampire)<256)threaten(m,vampire);
        if(m instanceof Villager&&night(w))for(var p:w.players())if(p.isAlive()&&!p.isSpectator()&&m.distanceToSqr(p)<64&&RpgAbilities.profile(p).rpgClass()==RpgClass.VAMPIRESCO){threaten(m,p);break;}
        if(!wounded){m.getPersistentData().remove("oxy_bandaging_since");m.getPersistentData().remove("oxy_wound_seen");return;}
        boolean capable=(m instanceof Villager||m instanceof WanderingTrader||m instanceof AbstractIllager||m instanceof net.minecraft.world.entity.monster.piglin.Piglin)&&Math.floorMod(m.getUUID().hashCode(),3)==0;
        if(!capable)return;var tag=m.getPersistentData();long now=w.getGameTime();
        if(!tag.contains("oxy_wound_seen"))tag.putLong("oxy_wound_seen",now);
        if(now-tag.getLongOr("oxy_wound_seen",now)<120||tag.getIntOr("oxy_bandages_used",0)>=2)return;
        if(m.getTarget()!=null&&m.distanceToSqr(m.getTarget())<16){tag.remove("oxy_bandaging_since");return;}
        if(!tag.contains("oxy_bandaging_since")){tag.putLong("oxy_bandaging_since",now);return;}
        if(now-tag.getLongOr("oxy_bandaging_since",now)<40||m.hurtTime>0)return;
        tag.putInt("oxy_bandages_used",tag.getIntOr("oxy_bandages_used",0)+1);tag.remove("oxy_bandaging_since");Bleeding.stop(m);m.heal(2);
        w.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,m.getX(),m.getY()+m.getBbHeight(),m.getZ(),3,.2,.15,.2,.02);
        for(var viewer:w.players())if(viewer.distanceToSqr(m)<1024)CombatFeedback.text(viewer,m,"Bandagem",0xA9E3BF);
    }
}
