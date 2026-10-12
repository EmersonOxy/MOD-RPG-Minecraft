package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Carried creatures follow below the player without becoming a vehicle/passenger. */
public final class AviaryCarry {
    private record Lock(Mob mob,long until) {}
    private static final Map<UUID,Lock> LOCKS=new HashMap<>();
    private static final Map<UUID,Mob> CARGO=new HashMap<>();
    private static final Map<UUID,ServerPlayer> CARRIERS=new HashMap<>();
    private static final Map<UUID,Long> APPROACH=new HashMap<>();
    private static final String RESTORE="oxy_carry_restore";
    private AviaryCarry() {}
    public static void beginApproach(ServerPlayer p){var lock=LOCKS.get(p.getUUID());if(lock!=null&&lock.until>p.level().getGameTime()){
        APPROACH.put(p.getUUID(),p.level().getGameTime()+120);p.getPersistentData().remove("oxy_landing_until");PacketDistributor.sendToPlayer(p,new RpgNetwork.CarryApproach(lock.mob.getId(),120));
    }}
    public static void clientApproach(Player p,int mob,int ticks){var tag=p.getPersistentData();tag.putInt("oxy_approach_mob",mob);tag.putInt("oxy_approach_until",p.tickCount+Math.clamp(ticks,0,120));tag.remove("oxy_landing_until");}
    public static boolean approaching(Player p){
        if(p instanceof ServerPlayer)return APPROACH.getOrDefault(p.getUUID(),0L)>p.level().getGameTime()&&LOCKS.containsKey(p.getUUID())&&!CARGO.containsKey(p.getUUID());
        return p.isLocalPlayer()&&EquipmentAccess.clientSelected()==RpgClass.AVIARIO&&p.getPersistentData().getIntOr("oxy_approach_until",0)>p.tickCount;
    }
    public static boolean approachMovement(Player p){
        if(!approaching(p)||!p.isFallFlying()||RpgAviary.featherFalling(p))return false;
        if(p.level().getEntity(p.getPersistentData().getIntOr("oxy_approach_mob",-1)) instanceof Mob mob&&mob.isAlive()&&p.distanceToSqr(mob)<=72*72&&p.hasLineOfSight(mob)){
            p.setDeltaMovement(approachVelocity(p,mob));return true;
        }return false;
    }
    private static Vec3 approachVelocity(Player p,Mob mob){var to=mob.position().add(0,mob.getBbHeight()+.6,0).subtract(p.position());return to.normalize().scale(Math.min(1.1,to.length()*.3));}
    public static boolean lifting(Player p){return p.getPersistentData().getLongOr("oxy_carry_lift_until",0)>p.level().getGameTime()&&p.getPersistentData().getIntOr("oxy_carried_mob",-1)>=0;}
    public static boolean liftMovement(Player p){if(!lifting(p)||!p.isFallFlying())return false;var v=p.getDeltaMovement();p.setDeltaMovement(v.x*.96,Math.max(.45,v.y),v.z*.96);return true;}
    private static void cancelApproach(ServerPlayer p){if(APPROACH.remove(p.getUUID())!=null)PacketDistributor.sendToPlayer(p,new RpgNetwork.CarryApproach(-1,0));}
    private static boolean eligible(Mob m){return RpgAbilities.attackable(m)&&m.getType()!=EntityType.ENDER_DRAGON&&m.getType()!=EntityType.WITHER&&m.getBbWidth()<=3&&m.getBbHeight()<=4&&!CARRIERS.containsKey(m.getUUID());}
    public static boolean holding(Player player,Entity mob){return player!=null&&mob instanceof Mob&&mob.level()==player.level()&&player.getPersistentData().getIntOr("oxy_carried_mob",-1)==mob.getId();}
    public static Vec3 position(Player p,Entity mob){return p.position().add(0,-mob.getBbHeight()-.25,0);}
    public static void request(ServerPlayer p,int id){
        if(id<0){release(p);LOCKS.remove(p.getUUID());APPROACH.remove(p.getUUID());p.getPersistentData().putLong("oxy_carry_wait",p.level().getGameTime()+40);return;}
        if(RpgAbilities.profile(p).rpgClass()!=RpgClass.AVIARIO||!p.isAlive()||p.isSpectator()||CARGO.containsKey(p.getUUID()))return;
        var entity=p.level().getEntity(id);if(!(entity instanceof Mob m)||!eligible(m)||p.distanceToSqr(m)>Math.pow(TargetingRange.of(p),2)||!p.hasLineOfSight(m))return;
        var old=LOCKS.put(p.getUUID(),new Lock(m,p.level().getGameTime()+20));
        if(approaching(p)&&(old==null||old.mob!=m))PacketDistributor.sendToPlayer(p,new RpgNetwork.CarryApproach(id,(int)Math.max(0,APPROACH.get(p.getUUID())-p.level().getGameTime())));
    }
    private static void sync(ServerPlayer p,int id){p.getPersistentData().putInt("oxy_carried_mob",id);PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new RpgNetwork.CarryState(p.getId(),id));}
    /** Restore original AI/gravity even if a carried creature came from an interrupted autosave. */
    private static void restore(Mob mob){
        var tag=mob.getPersistentData();if(!tag.getBooleanOr(RESTORE,false))return;
        mob.setNoAi(tag.getBooleanOr("oxy_carry_ai",false));mob.setNoGravity(tag.getBooleanOr("oxy_carry_gravity",false));mob.noPhysics=tag.getBooleanOr("oxy_carry_physics",false);
        for(String key:List.of(RESTORE,"oxy_carry_ai","oxy_carry_gravity","oxy_carry_physics"))tag.remove(key);
    }
    public static void join(EntityJoinLevelEvent e){if(!e.getLevel().isClientSide()&&e.getEntity() instanceof Mob m&&!CARRIERS.containsKey(m.getUUID()))restore(m);}
    private static void capture(ServerPlayer p,Mob mob){
        var tag=mob.getPersistentData();tag.putBoolean(RESTORE,true);tag.putBoolean("oxy_carry_ai",mob.isNoAi());tag.putBoolean("oxy_carry_gravity",mob.isNoGravity());tag.putBoolean("oxy_carry_physics",mob.noPhysics);
        mob.setNoAi(true);mob.setNoGravity(true);mob.noPhysics=true;mob.getNavigation().stop();mob.setTarget(null);
        CARGO.put(p.getUUID(),mob);CARRIERS.put(mob.getUUID(),p);APPROACH.remove(p.getUUID());p.getPersistentData().putLong("oxy_carry_lift_until",p.level().getGameTime()+16);p.getPersistentData().remove("oxy_landing_until");sync(p,mob.getId());liftMovement(p);follow(p,mob);
    }
    private static void follow(Player p,Mob mob){mob.setPos(position(p,mob));mob.setDeltaMovement(Vec3.ZERO);mob.resetFallDistance();}
    private static void release(ServerPlayer p){
        var mob=CARGO.remove(p.getUUID());if(mob!=null){
            CARRIERS.remove(mob.getUUID());restore(mob);mob.resetFallDistance();var v=p.getDeltaMovement();mob.setDeltaMovement(v.x*.35,Math.min(-.12,v.y),v.z*.35);mob.hurtMarked=true;
            if(mob.level() instanceof net.minecraft.server.level.ServerLevel level)level.getChunkSource().sendToTrackingPlayers(mob,new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(mob));
        }p.getPersistentData().remove("oxy_carry_lift_until");sync(p,-1);
    }
    /** Client counterpart of the transport packet; entities keep their ordinary identity. */
    public static void receive(Player p,int id){
        p.getPersistentData().remove("oxy_approach_mob");p.getPersistentData().remove("oxy_approach_until");
        var old=p.level().getEntity(p.getPersistentData().getIntOr("oxy_carried_mob",-1));
        if(id>=0&&(old==null||old.getId()!=id)){p.getPersistentData().putLong("oxy_carry_lift_until",p.level().getGameTime()+16);p.getPersistentData().remove("oxy_landing_until");}
        else if(id<0)p.getPersistentData().remove("oxy_carry_lift_until");
        if(old instanceof Mob mob&&old.getId()!=id){mob.noPhysics=mob.getPersistentData().getBooleanOr("oxy_carry_client_physics",false);mob.getPersistentData().remove("oxy_carry_player");mob.getPersistentData().remove("oxy_carry_client_physics");}
        p.getPersistentData().putInt("oxy_carried_mob",id);
        if(p.level().getEntity(id) instanceof Mob mob){var tag=mob.getPersistentData();if(!tag.contains("oxy_carry_player"))tag.putBoolean("oxy_carry_client_physics",mob.noPhysics);tag.putInt("oxy_carry_player",p.getId());mob.noPhysics=true;follow(p,mob);}
    }
    public static void tick(EntityTickEvent.Post e){
        if(e.getEntity() instanceof Mob mob){
            if(mob.level().isClientSide()){
                var entity=mob.level().getEntity(mob.getPersistentData().getIntOr("oxy_carry_player",-1));
                if(entity instanceof Player p&&holding(p,mob))follow(p,mob);
                else if(mob.getPersistentData().contains("oxy_carry_player")){mob.noPhysics=mob.getPersistentData().getBooleanOr("oxy_carry_client_physics",false);mob.getPersistentData().remove("oxy_carry_player");mob.getPersistentData().remove("oxy_carry_client_physics");}
            }else{var p=CARRIERS.get(mob.getUUID());if(p!=null&&holding(p,mob))follow(p,mob);}
            return;
        }
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        long now=p.level().getGameTime();var cargo=CARGO.get(p.getUUID());var lock=LOCKS.get(p.getUUID());
        boolean flying=RpgAbilities.profile(p).rpgClass()==RpgClass.AVIARIO&&p.isAlive()&&!p.isSpectator()&&!p.onGround()&&!p.isPassenger()&&!p.isInWater()&&!p.isInLava()&&(p.isFallFlying()||p.getAbilities().flying);
        if(cargo!=null){
            // Feather Fall stops elytra animation; it does not mean the cargo was released.
            boolean carrying=flying||RpgAviary.featherFalling(p);
            if(!carrying||!cargo.isAlive()||cargo.isRemoved()||cargo.level()!=p.level()||cargo.isPassenger()||!p.level().noCollision(cargo,cargo.getDimensions(cargo.getPose()).makeBoundingBox(position(p,cargo)))){release(p);LOCKS.remove(p.getUUID());p.getPersistentData().putLong("oxy_carry_wait",now+40);return;}
            if(liftMovement(p)&&p.tickCount%2==0)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(p));follow(p,cargo);if(p.tickCount%10==0)sync(p,cargo.getId());return;
        }
        if(!flying||p.getPersistentData().getLongOr("oxy_carry_wait",0)>now){cancelApproach(p);return;}
        if(lock==null||lock.until<=now||!lock.mob.isAlive()||lock.mob.isRemoved()||lock.mob.level()!=p.level()){
            LOCKS.remove(p.getUUID());
            var nearby=p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(3.5),m->eligible(m)&&!m.isPassenger()&&!m.isVehicle()&&p.distanceToSqr(m)<=3.5*3.5&&p.hasLineOfSight(m));var aim=AbilityAim.direction(p);
            lock=nearby.stream().filter(m->m.getBoundingBox().getCenter().subtract(p.getEyePosition()).normalize().dot(aim)>.15).min(Comparator.comparingDouble(p::distanceToSqr)).map(m->new Lock(m,now+40)).orElse(null);
            if(lock!=null)LOCKS.put(p.getUUID(),lock);
        }
        if(lock==null){cancelApproach(p);return;}var mob=lock.mob;
        if(!eligible(mob)||mob.isPassenger()||mob.isVehicle()||!p.hasLineOfSight(mob)){cancelApproach(p);return;}
        if(APPROACH.getOrDefault(p.getUUID(),0L)>now){
            p.setDeltaMovement(approachVelocity(p,mob));p.hurtMarked=true;
            if(p.tickCount%2==0)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(p));
        }
        if(p.distanceToSqr(mob)<=3.5*3.5&&p.level().noCollision(mob,mob.getDimensions(mob.getPose()).makeBoundingBox(position(p,mob))))capture(p,mob);
    }
    public static void tracking(PlayerEvent.StartTracking e){if(e.getEntity() instanceof ServerPlayer viewer){
        if(e.getTarget() instanceof ServerPlayer p&&CARGO.containsKey(p.getUUID()))PacketDistributor.sendToPlayer(viewer,new RpgNetwork.CarryState(p.getId(),CARGO.get(p.getUUID()).getId()));
        else if(e.getTarget() instanceof Mob mob){var p=CARRIERS.get(mob.getUUID());if(p!=null)PacketDistributor.sendToPlayer(viewer,new RpgNetwork.CarryState(p.getId(),mob.getId()));}
    }}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){release(p);LOCKS.remove(p.getUUID());APPROACH.remove(p.getUUID());}}
    public static void stop(ServerStoppedEvent e){for(var p:new ArrayList<>(CARRIERS.values()))release(p);LOCKS.clear();CARGO.clear();CARRIERS.clear();APPROACH.clear();}
}
