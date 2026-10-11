package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server owns duration, costs and impacts; the local player simulates the same glide as vanilla. */
public final class RpgAviary {
    private static final Map<UUID,Flight> FLIGHTS=new HashMap<>();
    private static class Flight {
        long until,boostUntil,meteorUntil,launchAt,lastBoost;int total;double peak,speed,thrust;Vec3 previous;RpgProfile cast;boolean launched,slowUsed;Object dimension;
    }
    private RpgAviary(){}
    public static boolean boostAvailable(ServerPlayer p,int slot){var f=FLIGHTS.get(p.getUUID());return slot==0&&f!=null&&f.until>p.level().getGameTime()&&f.meteorUntil==0;}
    public static boolean canFly(LivingEntity entity){
        if(!(entity instanceof Player p)||!p.isAlive()||p.onGround()||p.isPassenger()||p.isInWater()||p.isInLava()||p.hasEffect(MobEffects.LEVITATION)||p.getAbilities().flying)return false;
        if(p instanceof ServerPlayer s){var f=FLIGHTS.get(p.getUUID());return f!=null&&f.until>s.level().getGameTime()&&f.meteorUntil==0;}
        return p.getPersistentData().getIntOr("oxy_flight_until",0)>p.tickCount&&p.getPersistentData().getIntOr("oxy_meteor_until",0)<=p.tickCount;
    }
    public static void validate(ServerPlayer p,int slot){
        if(slot==1)throw new IllegalStateException("Peso pena é passiva: agache enquanto cai para descer lentamente.");
        if(slot==2&&(p.onGround()||p.isInWater()||p.isInLava()||p.isPassenger()||p.getAbilities().flying))throw new IllegalStateException("Impacto Meteoro exige estar voando ou caindo no ar.");
        if(slot==0&&(p.isInWater()||p.isInLava()||p.isPassenger()||p.hasEffect(MobEffects.LEVITATION)||p.getAbilities().flying))throw new IllegalStateException("Decole fora da água, sem montaria e com o voo criativo desativado.");
        var f=FLIGHTS.get(p.getUUID());if(f!=null&&f.meteorUntil>0)throw new IllegalStateException("Impacto Meteoro já está preparado: aguarde o pouso.");
        if(slot==0&&f!=null&&f.lastBoost>0&&p.level().getGameTime()-f.lastBoost<10)throw new IllegalStateException("Aguarde meio segundo entre impulsos.");
    }
    public static void activate(ServerPlayer p,RpgProfile profile,int slot){
        var f=FLIGHTS.computeIfAbsent(p.getUUID(),id->new Flight());long now=p.level().getGameTime();f.dimension=p.level().dimension();f.cast=profile;
        if(slot==0){
            if(f.until>now){f.lastBoost=now;f.thrust=Math.min(5.2,Math.max(profile.hasSkill(6)?3.8:3.3,Math.max(p.getKnownMovement().length(),f.thrust)+1.2));p.setDeltaMovement(p.getLookAngle().scale(f.thrust));p.hurtMarked=true;f.boostUntil=Math.min(f.until,now+(profile.mainUnlocked(0)?80:40)+(profile.hasSkill(5)?20:0));}
            else{f.thrust=0;f.total=profile.abilityDuration(0);f.until=now+f.total;f.launched=false;f.launchAt=now;p.stopFallFlying();p.setDeltaMovement(p.getDeltaMovement().x,1.25,p.getDeltaMovement().z);p.hurtMarked=true;ClassJourney.advance(p,0,1);}
            p.level().playSound(null,p.blockPosition(),SoundEvents.FIREWORK_ROCKET_LAUNCH,SoundSource.PLAYERS,.7f,1.2f);
        }else{
            f.until=f.boostUntil=0;f.meteorUntil=now+600;f.peak=p.getY();f.previous=p.position();f.speed=p.getKnownMovement().length();
            p.stopFallFlying();p.setDeltaMovement(p.getDeltaMovement().scale(.5).add(0,-1.2,0));p.hurtMarked=true;
        }
        sync(p,f);
    }
    private static void sync(ServerPlayer p,Flight f){long now=p.level().getGameTime();PacketDistributor.sendToPlayer(p,new RpgNetwork.FlightState((int)Math.max(0,f.until-now),(int)Math.max(0,f.boostUntil-now),(int)Math.max(0,f.meteorUntil-now),f.cast!=null&&f.cast.hasSkill(6),(float)f.thrust));}
    public static int remaining(ServerPlayer p,int slot){var f=FLIGHTS.get(p.getUUID());if(f==null)return 0;return slot==0?(int)Math.max(0,f.until-p.level().getGameTime()):slot==2?(int)Math.max(0,f.meteorUntil-p.level().getGameTime()):0;}
    public static void tick(EntityTickEvent.Pre event){
        if(!(event.getEntity() instanceof Player player))return;
        if(player.level().isClientSide()){
            if(!player.isLocalPlayer())return;var tag=player.getPersistentData();
            if(tag.getIntOr("oxy_meteor_until",0)>player.tickCount&&!player.onGround())player.setDeltaMovement(player.getDeltaMovement().multiply(.96,1,.96).add(0,-.12,0).with(net.minecraft.core.Direction.Axis.Y,Math.max(-3,player.getDeltaMovement().y-.12)));
            else if(tag.getIntOr("oxy_boost_until",0)>player.tickCount&&canFly(player)){
                var direction=player.getLookAngle();double speed=Math.clamp(tag.getFloatOr("oxy_flight_thrust",3.3f),3.3,5.2);player.setDeltaMovement(player.getDeltaMovement().scale(.72).add(direction.scale(speed*.28)));
            }
            return;
        }
        if(!(player instanceof ServerPlayer p))return;var profile=RpgAbilities.profile(p);var f=FLIGHTS.get(p.getUUID());
        if(profile.rpgClass()!=RpgClass.AVIARIO||!p.isAlive()||p.isSpectator()){if(f!=null)clear(p);return;}
        boolean slow=slowing(p,profile);
        if(slow){p.stopFallFlying();if(!p.hasEffect(MobEffects.SLOW_FALLING))p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,4,0,false,false,true));
            if(profile.hasSkill(11)&&p.getKnownMovement().y<-.07){p.setDeltaMovement(p.getKnownMovement().with(net.minecraft.core.Direction.Axis.Y,-.07));p.hurtMarked=true;}}
        if(f==null){f=new Flight();f.dimension=p.level().dimension();FLIGHTS.put(p.getUUID(),f);}
        if(slow&&!f.slowUsed){f.slowUsed=true;ClassJourney.advance(p,1,1);}if(p.onGround())f.slowUsed=false;
        if(f.dimension!=p.level().dimension()||p.isPassenger()||p.isInWater()||p.isInLava()){clear(p);return;}
        long now=p.level().getGameTime();
        if(f.meteorUntil>0){
            if(now>=f.meteorUntil){f.meteorUntil=0;sync(p,f);}
            else{
                double travel=f.previous==null?0:p.position().distanceTo(f.previous);if(travel>64){clear(p);return;}
                f.speed=Math.max(f.speed,Math.min(8,travel));f.peak=Math.max(f.peak,p.getY());f.previous=p.position();
                if(p.onGround()){impact(p,f);f.meteorUntil=0;sync(p,f);}
            }
        }
        if(f.until>0){
            if(now>=f.until||(f.launched&&p.onGround())){f.until=f.boostUntil=0;p.stopFallFlying();sync(p,f);}
            else if(!f.launched&&!p.onGround()&&now-f.launchAt>=6&&canFly(p)){f.launched=true;p.startFallFlying();sync(p,f);}
        }
        if(now%10==0&&(f.until>now||f.meteorUntil>now))sync(p,f);
    }
    private static boolean slowing(ServerPlayer p,RpgProfile s){var f=FLIGHTS.get(p.getUUID());return s.rpgClass()==RpgClass.AVIARIO&&s.hasSkill(8)&&!p.onGround()&&p.isShiftKeyDown()&&p.getKnownMovement().y<0&&!p.isInWater()&&(f==null||f.meteorUntil==0);}
    public static void fall(LivingFallEvent event){if(event.getEntity() instanceof ServerPlayer p){var s=RpgAbilities.profile(p);if(s.rpgClass()!=RpgClass.AVIARIO)return;var f=FLIGHTS.get(p.getUUID());if(f!=null&&f.meteorUntil>0)event.setCanceled(true);}}
    public static void damage(LivingDamageEvent.Pre event){if(event.getEntity() instanceof ServerPlayer p){var s=RpgAbilities.profile(p);if(s.rpgClass()==RpgClass.AVIARIO&&event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL))event.setNewDamage(event.getNewDamage()*.7f);if(event.getSource().getEntity() instanceof LivingEntity&&slowing(p,s)){float reduction=(s.mainUnlocked(1)?.25f:s.hasSkill(9)?.1f:0)+(s.hasSkill(12)?.1f:0)+(s.hasSkill(13)?.1f:0);event.setNewDamage(event.getNewDamage()*(1-reduction));}}}
    private static void impact(ServerPlayer p,Flight f){
        var level=p.level();var s=f.cast;double force=Math.min(12,Math.sqrt(Math.max(0,f.peak-p.getY()))*.38+f.speed*f.speed*.35);
        double radius=Math.min(14,(s.mainUnlocked(2)?3:2)+force*.8+(s.hasSkill(18)?1:0)+(s.hasSkill(20)?1:0));
        float damage=(float)((s.mainUnlocked(2)?8:4)*(1+force)*(s.hasSkill(16)?1.15:1)*(s.hasSkill(19)?1.2:1)*(s.hasSkill(14)?1.05:1));int hits=0;
        for(var mob:level.getEntitiesOfClass(LivingEntity.class,new AABB(p.position(),p.position()).inflate(radius),RpgAbilities::attackable))if(mob.distanceToSqr(p)<=radius*radius){if(mob.hurtServer(level,p.damageSources().playerAttack(p),damage))hits++;var away=mob.position().subtract(p.position()).normalize();mob.push(away.x*.7,.5,away.z*.7);}
        if(hits>0)ClassJourney.advance(p,2,1);
        if(s.mainUnlocked(2)&&p.mayBuild())MeteorCrater.enqueue(p,radius,force,s.hasSkill(20));
        level.sendParticles(ParticleTypes.EXPLOSION,p.getX(),p.getY()+.2,p.getZ(),8,radius*.4,.3,radius*.4,0);
        level.playSound(null,p.blockPosition(),SoundEvents.GENERIC_EXPLODE.value(),SoundSource.PLAYERS,1.2f,.75f);p.fallDistance=0;
    }
    public static void clear(ServerPlayer p){if(FLIGHTS.remove(p.getUUID())!=null){p.stopFallFlying();PacketDistributor.sendToPlayer(p,new RpgNetwork.FlightState(0,0,0,false));}}
    public static void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent event){FLIGHTS.clear();}
}
