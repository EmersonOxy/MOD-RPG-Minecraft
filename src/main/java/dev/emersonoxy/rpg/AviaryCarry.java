package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** One creature can be picked up at close range; release returns to ordinary falling physics. */
public final class AviaryCarry {
    private record Lock(Mob mob,long until) {}
    private static final Map<UUID,Lock> LOCKS=new HashMap<>();
    private static final Map<UUID,Mob> CARGO=new HashMap<>();
    private AviaryCarry() {}
    public static boolean holding(Player player,Entity mob){return player!=null&&mob instanceof Mob&&mob.getVehicle()==player&&player.getPersistentData().getIntOr("oxy_carried_mob",-1)==mob.getId();}
    public static void request(ServerPlayer p,int id){
        if(id<0){release(p);LOCKS.remove(p.getUUID());return;}
        if(RpgAbilities.profile(p).rpgClass()!=RpgClass.AVIARIO||!p.isAlive()||p.isSpectator())return;
        var entity=p.level().getEntity(id);if(!(entity instanceof Mob m)||!RpgAbilities.attackable(m)||p.distanceToSqr(m)>24*24||m.getType()==EntityType.ENDER_DRAGON||m.getType()==EntityType.WITHER)return;
        LOCKS.put(p.getUUID(),new Lock(m,p.level().getGameTime()+20));
    }
    private static void sync(ServerPlayer p,int id){p.getPersistentData().putInt("oxy_carried_mob",id);PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new RpgNetwork.CarryState(p.getId(),id));}
    private static void release(ServerPlayer p){
        var mob=CARGO.remove(p.getUUID());if(mob==null)return;
        if(mob.getVehicle()==p){mob.stopRiding();mob.resetFallDistance();var v=p.getDeltaMovement();mob.setDeltaMovement(v.x*.35,Math.min(-.12,v.y),v.z*.35);mob.hurtMarked=true;}
        sync(p,-1);
    }
    public static void tick(EntityTickEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var lock=LOCKS.get(p.getUUID());var cargo=CARGO.get(p.getUUID());
        boolean flying=RpgAbilities.profile(p).rpgClass()==RpgClass.AVIARIO&&p.isAlive()&&!p.isSpectator()&&!p.onGround()&&!p.isPassenger()&&!p.isInWater()&&!p.isInLava()&&(p.isFallFlying()||p.getAbilities().flying);
        if(!flying||lock==null||lock.until<=p.level().getGameTime()||!lock.mob.isAlive()||lock.mob.level()!=p.level()){
            release(p);if(lock!=null&&lock.until<=p.level().getGameTime())LOCKS.remove(p.getUUID());return;
        }
        var mob=lock.mob;
        if(cargo!=null&&cargo!=mob){release(p);cargo=null;}
        if(cargo!=null&&!holding(p,cargo)){release(p);return;}
        if(cargo!=null&&p.tickCount%20==0)sync(p,cargo.getId());
        if(cargo==null&&p.distanceToSqr(mob)<=3.5*3.5&&p.hasLineOfSight(mob)&&!mob.isPassenger()&&!mob.isVehicle()&&p.getPassengers().isEmpty()){
            var below=p.position().add(0,-mob.getBbHeight()-.2,0);
            if(p.level().noCollision(mob,mob.getDimensions(mob.getPose()).makeBoundingBox(below))&&mob.startRiding(p,true,true)){
                CARGO.put(p.getUUID(),mob);mob.resetFallDistance();sync(p,mob.getId());
            }
        }
    }
    public static void tracking(PlayerEvent.StartTracking e){if(e.getEntity() instanceof ServerPlayer viewer&&e.getTarget() instanceof ServerPlayer p&&CARGO.containsKey(p.getUUID()))PacketDistributor.sendToPlayer(viewer,new RpgNetwork.CarryState(p.getId(),CARGO.get(p.getUUID()).getId()));}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){release(p);LOCKS.remove(p.getUUID());}}
    public static void stop(ServerStoppedEvent e){for(var world:e.getServer().getAllLevels())for(var p:world.players())release(p);LOCKS.clear();CARGO.clear();}
}
