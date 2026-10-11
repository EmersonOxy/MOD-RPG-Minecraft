package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.entity.projectile.ThrownSplashPotion;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Vanilla visual lightning and splash potions, with authoritative hand/mana/timer checks. */
public final class RpgShaman {
    private static final class Active {
        ServerLevel world;final long[] until=new long[3];final RpgProfile[] profile=new RpgProfile[3];long lightning,potion;
    }
    private static final Map<UUID,Active> ACTIVE=new HashMap<>();
    private RpgShaman(){}
    public static void activate(ServerPlayer p,RpgProfile s,int slot){
        var a=ACTIVE.computeIfAbsent(p.getUUID(),id->new Active());
        a.world=(ServerLevel)p.level();a.profile[slot]=s;a.until[slot]=p.level().getGameTime()+s.abilityDuration(slot);
        if(slot==0)a.lightning=0;
        if(slot==1){RpgNecromancer.animals(p,s,s.mainUnlocked(1)?3+(s.hasSkill(13)?1:0):1);pulse(p,s);}
        if(slot==2){a.potion=0;p.sendSystemMessage(Component.literal("[Oxy RPG] Especialista: mãos vazias, esquerdo negativo / direito positivo."));}
        ((ServerLevel)p.level()).sendParticles(slot==1?ParticleTypes.HEART:ParticleTypes.ENCHANT,p.getX(),p.getY()+1,p.getZ(),15,.5,.6,.5,.03);
    }
    public static boolean expertise(ServerPlayer p){var a=ACTIVE.get(p.getUUID());return a!=null&&a.until[2]>p.level().getGameTime();}
    public static void attack(AttackEntityEvent event){
        if(event.isCanceled()||!(event.getEntity() instanceof ServerPlayer p)||!p.getMainHandItem().isEmpty()
            ||!(event.getTarget() instanceof LivingEntity enemy)||!RpgAbilities.attackable(enemy))return;
        var a=ACTIVE.get(p.getUUID());long time=p.level().getGameTime();
        if(a==null||a.until[0]<=time||time<a.lightning)return;a.lightning=time+30;
        var s=a.profile[0];var world=(ServerLevel)p.level();
        var bolt=EntityType.LIGHTNING_BOLT.create(world,EntitySpawnReason.TRIGGERED);
        if(bolt==null)return;bolt.setPos(enemy.position());bolt.setVisualOnly(true);bolt.setCause(p);world.addFreshEntity(bolt);
        float damage=((s.mainUnlocked(0)?6:3)+(s.hasSkill(2)?1:0)+(s.hasSkill(5)?2:0))*1.05f*(1+ClassEquipment.active(p,RpgClass.XAMA)*.05f);
        var source=new net.minecraft.world.damagesource.DamageSource(p.damageSources().lightningBolt().typeHolder(),bolt,p);
        enemy.hurtServer(world,source,damage);
        if(s.hasSkill(6))for(var next:world.getEntitiesOfClass(LivingEntity.class,enemy.getBoundingBox().inflate(2),e->RpgAbilities.attackable(e)&&e!=enemy)){
            if(p.hasLineOfSight(next)){next.hurtServer(world,source,damage*.5f);break;}
        }
    }
    public static void potion(ServerPlayer p,boolean positive){
        var a=ACTIVE.get(p.getUUID());long time=p.level().getGameTime();
        if(a==null||a.until[2]<=time||!p.getMainHandItem().isEmpty()||!p.getOffhandItem().isEmpty())return;
        if(time<a.potion)return;
        var s=a.profile[2];int cost=ClassEquipment.discount(p,RpgClass.XAMA,(s.mainUnlocked(2)?5:4)-(s.hasSkill(20)?1:0));
        if(!RpgResources.affordable(p,cost)){p.sendSystemMessage(Component.literal("[Oxy RPG] Mana insuficiente: precisa de "+cost+" MP para a poção."));a.potion=time+10;return;}
        a.potion=time+(RpgDebug.unlimited(p)?1:s.hasSkill(16)?15:20);
        List<Holder<MobEffect>> pool=positive?List.of(MobEffects.INSTANT_HEALTH,MobEffects.REGENERATION,MobEffects.SPEED,MobEffects.STRENGTH,MobEffects.FIRE_RESISTANCE,MobEffects.RESISTANCE)
            :List.of(MobEffects.INSTANT_DAMAGE,MobEffects.POISON,MobEffects.SLOWNESS,MobEffects.WEAKNESS);
        var effect=pool.get(p.getRandom().nextInt(pool.size()));
        int duration=s.mainUnlocked(2)?200+(s.hasSkill(19)?100:0):80;
        var stack=new ItemStack(Items.SPLASH_POTION);
        stack.set(DataComponents.POTION_CONTENTS,new PotionContents(Optional.empty(),Optional.empty(),List.of(new MobEffectInstance(effect,duration,0)),Optional.empty()));
        var shot=new ThrownSplashPotion(p.level(),p,stack);
        AbilityAim.shoot(shot,p,1.05f);
        RpgResources.spend(p,cost);p.level().addFreshEntity(shot);ClassJourney.advance(p,2,1);p.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);RpgResources.sync(p);
    }
    private static void pulse(ServerPlayer p,RpgProfile s){
        var world=(ServerLevel)p.level();double radius=(s.mainUnlocked(1)?4:2)+(s.hasSkill(11)?1:0);
        float heal=((s.mainUnlocked(1)?1:.5f)+(s.hasSkill(12)?.5f:0))*(1+ClassEquipment.active(p,RpgClass.XAMA)*.05f);
        for(var patient:world.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(radius),e->e.isAlive()&&(!(e instanceof Enemy)||RpgNecromancer.isSummon(e))&&p.distanceToSqr(e)<=radius*radius)){
            if(!p.hasLineOfSight(patient)&&patient!=p)continue;
            float before=patient.getHealth();patient.heal(heal);if(patient.getHealth()>before)ClassJourney.advance(p,1,1);
            for(var effect:patient.getActiveEffects().stream().filter(e->e.getEffect().value().getCategory()==MobEffectCategory.HARMFUL).map(MobEffectInstance::getEffect).toList())patient.removeEffect(effect);
            RpgAbilities.cleanse(patient);
            world.sendParticles(ParticleTypes.HEART,patient.getX(),patient.getY()+1,patient.getZ(),2,.2,.2,.2,.02);
        }
        var origin=p.blockPosition().below();var meal=new ItemStack(Items.BONE_MEAL,64);
        int grown=0;
        for(int i=0;i<32&&grown<4;i++){
            var pos=origin.offset(p.getRandom().nextInt(7)-3,p.getRandom().nextInt(3),p.getRandom().nextInt(7)-3);
            if(world.hasChunkAt(pos)&&world.mayInteract(p,pos)&&BoneMealItem.growCrop(meal,world,pos)){world.levelEvent(1505,pos,0);grown++;}
        }
        world.sendParticles(ParticleTypes.HAPPY_VILLAGER,p.getX(),p.getY()+.5,p.getZ(),15,radius,.3,radius,.01);
    }
    public static void tick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        var a=ACTIVE.get(p.getUUID());if(a==null)return;
        if(!p.isAlive()||a.world!=p.level()){clear(p);return;}
        if(a.until[1]>p.level().getGameTime()&&p.tickCount%20==0)pulse(p,a.profile[1]);
    }
    public static void clear(ServerPlayer p){ACTIVE.remove(p.getUUID());}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p)clear(p);}
    public static void stop(ServerStoppedEvent event){ACTIVE.clear();}
}

