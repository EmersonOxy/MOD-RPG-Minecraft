package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server-owned charges. Test mode spends charges normally; only debug bypasses them. */
public final class RpgEvasion {
    public static final int RECHARGE=120;
    private static final Map<UUID,Pool> POOLS=new HashMap<>(),TEST_POOLS=new HashMap<>();
    private static final class Pool {float energy;int maximum;long last,dodge,lastUse=-100,motionUntil;Vec3 origin,direction;Pool(int maximum,long now){this.maximum=maximum;energy=maximum;last=now;}}
    private RpgEvasion(){}
    public static int maximum(ServerPlayer p){int level=RpgAbilities.profile(p).level();return RpgConfig.charges(level);}
    private static Pool pool(ServerPlayer p){
        long now=p.level().getGameTime();int max=maximum(p);boolean testing=RpgDebug.enabled(p);
        var pools=testing?TEST_POOLS:POOLS;
        var a=pools.computeIfAbsent(p.getUUID(),id->{var value=new Pool(max,now);if(!testing){var tag=p.getPersistentData();if(tag.contains("oxy_dodge_energy")){value.energy=Math.clamp(tag.getFloatOr("oxy_dodge_energy",max),0,max);value.last=tag.getLongOr("oxy_dodge_saved",now);}}return value;});
        if(max>a.maximum)a.energy+=max-a.maximum;a.maximum=max;
        if(now>a.last)a.energy=Math.min(max,a.energy+Math.min(now-a.last,(long)RpgConfig.recharge()*3)/ (float)RpgConfig.recharge());
        a.energy=Math.clamp(a.energy,0,max);a.last=now;return a;
    }
    private static void save(ServerPlayer p,Pool a){if(RpgDebug.enabled(p))return;p.getPersistentData().putFloat("oxy_dodge_energy",a.energy);p.getPersistentData().putLong("oxy_dodge_saved",a.last);}
    public static void cancelMotion(ServerPlayer p){var a=pool(p);a.origin=null;a.motionUntil=0;PacketDistributor.sendToPlayer(p,new RpgNetwork.EvasionMotion(p.getX(),p.getY(),p.getZ(),0));}
    public static void refill(ServerPlayer p){var a=pool(p);a.energy=a.maximum;sync(p,a);}
    public static float currentEnergy(ServerPlayer p){return pool(p).energy;}
    public static void use(ServerPlayer p,int direction){
        var a=pool(p);long now=p.level().getGameTime();boolean free=AdvancedClasses.freeRoll(p);
        if(!p.isAlive()||p.isSpectator()||p.isPassenger()||p.isSleeping()||!free&&!p.onGround()||p.isInWater()||p.isInLava()||!free&&p.isFallFlying())return;
        if(now-a.lastUse<8)return;
        if(!free&&!RpgDebug.cooldownsDisabled(p)&&a.energy<.9999f){p.displayClientMessage(Component.literal("Esquiva recarregando"),true);sync(p,a);return;}
        Vec3 dir=new Vec3(0,0,1).yRot((float)Math.toRadians(-p.getYRot())).yRot((float)(-Math.floorMod(direction,8)*Math.PI/4));
        // Vanilla collision handles the movement; the dodge never teleports through blocks.
        if(!p.level().noCollision(p,p.getBoundingBox().move(dir.scale(.5))))return;
        if(!free&&!RpgDebug.cooldownsDisabled(p))a.energy=Math.max(0,a.energy-1);a.lastUse=now;a.dodge=now+6;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new RpgNetwork.EntityMotion(p.getId(),6,Math.floorMod(direction,8)));
        a.origin=p.position();a.direction=dir;a.motionUntil=now+13;PacketDistributor.sendToPlayer(p,new RpgNetwork.EvasionMotion(p.getX(),p.getY(),p.getZ(),13));
        p.stopUsingItem();p.setDeltaMovement(dir.scale(.08).add(0,.08,0));p.hasImpulse=true;p.hurtMarked=true;
        p.connection.send(new ClientboundSetEntityMotionPacket(p));
        ((net.minecraft.server.level.ServerLevel)p.level()).playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,net.minecraft.sounds.SoundSource.PLAYERS,.45f,1.35f);
        if(free)ClassJourney.advance(p,2,1);save(p,a);sync(p,a);
    }
    public static Vec3 movement(net.minecraft.world.entity.player.Player p,Vec3 move){
        Vec3 origin,direction;double remaining;
        if(p instanceof ServerPlayer server){
            var pool=(RpgDebug.enabled(server)?TEST_POOLS:POOLS).get(p.getUUID());
            if(pool==null||pool.origin==null||p.level().getGameTime()>=pool.motionUntil)return move;
            origin=pool.origin;direction=pool.direction;remaining=pool.motionUntil-p.level().getGameTime();
        }else{
            if(!p.isLocalPlayer())return move;var tag=p.getPersistentData();remaining=tag.getIntOr("oxy_dodge_motion_until",0)-p.tickCount;
            if(remaining<=0)return move;
            origin=new Vec3(tag.getDoubleOr("oxy_dodge_x",p.getX()),tag.getDoubleOr("oxy_dodge_y",p.getY()),tag.getDoubleOr("oxy_dodge_z",p.getZ()));
            direction=new Vec3(tag.getDoubleOr("oxy_dodge_dx",0),0,tag.getDoubleOr("oxy_dodge_dz",1));
        }
        double t=Math.clamp((14-remaining)/13,0,1);
        double travel=3*(t-Math.sin(2*Math.PI*t)/(2*Math.PI));
        var desired=origin.add(direction.scale(travel));
        return new Vec3(desired.x-p.getX(),move.y,desired.z-p.getZ());
    }
    public static void incoming(LivingIncomingDamageEvent event){if(Bleeding.damage(event.getSource()))return;
        if(!(event.getEntity() instanceof ServerPlayer p))return;var a=pool(p);var source=event.getSource();
        if(p.level().getGameTime()>=a.dodge||source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        // Fall, lava, drowning and other environmental damage remain ordinary damage.
        if(source.getEntity()==null&&!source.is(DamageTypeTags.IS_PROJECTILE)&&!source.is(DamageTypeTags.IS_EXPLOSION))return;
        event.setCanceled(true);CombatFeedback.text(p,p,"Esquivou",0x75BFFF);
    }
    public static void sync(ServerPlayer p){sync(p,pool(p));}
    private static void sync(ServerPlayer p,Pool a){PacketDistributor.sendToPlayer(p,new RpgNetwork.EvasionState(a.maximum,RpgDebug.cooldownsDisabled(p)||AdvancedClasses.freeRoll(p)?a.maximum:a.energy,RpgConfig.recharge()));}
    public static void tick(PlayerTickEvent.Post event){if(event.getEntity() instanceof ServerPlayer p){var a=pool(p);if(p.tickCount%5==0)sync(p,a);if(p.tickCount%100==0)save(p,a);}}
    public static void clonePlayer(PlayerEvent.Clone event){if(event.getEntity() instanceof ServerPlayer p){POOLS.remove(p.getUUID());var old=event.getOriginal().getPersistentData();if(old.contains("oxy_dodge_energy")){p.getPersistentData().putFloat("oxy_dodge_energy",old.getFloatOr("oxy_dodge_energy",1));p.getPersistentData().putLong("oxy_dodge_saved",old.getLongOr("oxy_dodge_saved",p.level().getGameTime()));}}}
    public static void endTesting(ServerPlayer p){TEST_POOLS.remove(p.getUUID());}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p){var a=POOLS.remove(p.getUUID());if(a!=null&&!RpgDebug.enabled(p))save(p,a);TEST_POOLS.remove(p.getUUID());}}
    public static void stop(ServerStoppedEvent event){POOLS.clear();TEST_POOLS.clear();}
}
