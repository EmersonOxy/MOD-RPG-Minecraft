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
    private static final class Pool {float energy;int maximum;long last,dodge,lastUse=-100;Pool(int maximum,long now){this.maximum=maximum;energy=maximum;last=now;}}
    private RpgEvasion(){}
    public static int maximum(ServerPlayer p){int level=RpgAbilities.profile(p).level();return RpgConfig.charges(level);}
    private static Pool pool(ServerPlayer p){
        long now=p.level().getGameTime();int max=maximum(p);boolean testing=RpgDebug.testing(p);
        var pools=testing?TEST_POOLS:POOLS;
        var a=pools.computeIfAbsent(p.getUUID(),id->{var value=new Pool(max,now);if(!testing){var tag=p.getPersistentData();if(tag.contains("oxy_dodge_energy")){value.energy=Math.clamp(tag.getFloatOr("oxy_dodge_energy",max),0,max);value.last=tag.getLongOr("oxy_dodge_saved",now);}}return value;});
        if(max>a.maximum)a.energy+=max-a.maximum;a.maximum=max;
        if(now>a.last)a.energy=Math.min(max,a.energy+Math.min(now-a.last,(long)RpgConfig.recharge()*3)/ (float)RpgConfig.recharge());
        a.energy=Math.clamp(a.energy,0,max);a.last=now;return a;
    }
    private static void save(ServerPlayer p,Pool a){if(RpgDebug.testing(p))return;p.getPersistentData().putFloat("oxy_dodge_energy",a.energy);p.getPersistentData().putLong("oxy_dodge_saved",a.last);}
    public static float currentEnergy(ServerPlayer p){return pool(p).energy;}
    public static void use(ServerPlayer p,int direction){
        var a=pool(p);long now=p.level().getGameTime();
        if(!p.isAlive()||p.isSpectator()||p.isPassenger()||p.isSleeping()||!p.onGround()||p.isInWater()||p.isInLava()||p.isFallFlying())return;
        if(now-a.lastUse<8)return;
        if(!RpgDebug.unlimited(p)&&a.energy<.9999f){p.displayClientMessage(Component.literal("Esquiva recarregando"),true);sync(p,a);return;}
        Vec3 dir=new Vec3(0,0,1).yRot((float)Math.toRadians(-p.getYRot())).yRot((float)(-Math.floorMod(direction,8)*Math.PI/4));
        // Vanilla collision handles the movement; the dodge never teleports through blocks.
        if(!p.level().noCollision(p,p.getBoundingBox().move(dir.scale(.5))))return;
        if(!RpgDebug.unlimited(p))a.energy=Math.max(0,a.energy-1);a.lastUse=now;a.dodge=now+6;
        p.stopUsingItem();p.setDeltaMovement(dir.scale(1.25).add(0,.1,0));p.hasImpulse=true;p.hurtMarked=true;
        p.connection.send(new ClientboundSetEntityMotionPacket(p));
        ((net.minecraft.server.level.ServerLevel)p.level()).playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,net.minecraft.sounds.SoundSource.PLAYERS,.45f,1.35f);
        save(p,a);sync(p,a);
    }
    public static void incoming(LivingIncomingDamageEvent event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;var a=pool(p);var source=event.getSource();
        if(p.level().getGameTime()>=a.dodge||source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        // Fall, lava, drowning and other environmental damage remain ordinary damage.
        if(source.getEntity()==null&&!source.is(DamageTypeTags.IS_PROJECTILE)&&!source.is(DamageTypeTags.IS_EXPLOSION))return;
        event.setCanceled(true);CombatFeedback.text(p,p,"Esquivou",0x75BFFF);
    }
    public static void sync(ServerPlayer p){sync(p,pool(p));}
    private static void sync(ServerPlayer p,Pool a){PacketDistributor.sendToPlayer(p,new RpgNetwork.EvasionState(a.maximum,RpgDebug.unlimited(p)?a.maximum:a.energy,RpgConfig.recharge()));}
    public static void tick(PlayerTickEvent.Post event){if(event.getEntity() instanceof ServerPlayer p){var a=pool(p);if(p.tickCount%5==0)sync(p,a);if(p.tickCount%100==0)save(p,a);}}
    public static void clonePlayer(PlayerEvent.Clone event){if(event.getEntity() instanceof ServerPlayer p){POOLS.remove(p.getUUID());var old=event.getOriginal().getPersistentData();if(old.contains("oxy_dodge_energy")){p.getPersistentData().putFloat("oxy_dodge_energy",old.getFloatOr("oxy_dodge_energy",1));p.getPersistentData().putLong("oxy_dodge_saved",old.getLongOr("oxy_dodge_saved",p.level().getGameTime()));}}}
    public static void endTesting(ServerPlayer p){TEST_POOLS.remove(p.getUUID());}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p){var a=POOLS.remove(p.getUUID());if(a!=null&&!RpgDebug.testing(p))save(p,a);TEST_POOLS.remove(p.getUUID());}}
    public static void stop(ServerStoppedEvent event){POOLS.clear();TEST_POOLS.clear();}
}
