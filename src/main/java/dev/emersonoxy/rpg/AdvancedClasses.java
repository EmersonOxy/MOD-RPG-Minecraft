package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server-owned casts and guided native arrows. No forced chunk loads or terrain explosions. */
public final class AdvancedClasses {
    private static final Map<UUID,State> STATES=new HashMap<>();
    private static final Map<UUID,Mark> MARKS=new HashMap<>();
    private static final Map<UUID,Flight> ARROWS=new HashMap<>();
    private record Mark(Mob mob,long until){}
    private static final class State {
        final long[] until=new long[3];final RpgProfile[] cast=new RpgProfile[3];
        int burst,shot,running,heat;boolean overheated;RpgClass type;final ServerPlayer owner;
        State(ServerPlayer p,RpgClass type){owner=p;this.type=type;}
    }
    private static final class Flight {
        final AbstractArrow arrow;final ServerPlayer owner;final RpgProfile profile;final Mob target;
        final int mode;final long birth;boolean exploded;
        Flight(AbstractArrow a,ServerPlayer p,RpgProfile s,Mob target,int mode){arrow=a;owner=p;profile=s;this.target=target;this.mode=mode;birth=p.level().getGameTime();}
    }
    private AdvancedClasses(){}
    public static boolean cannotSwim(net.minecraft.world.entity.player.Player p){return p.isAlive()&&!p.hasInfiniteMaterials()&&p.isInWater()&&(p instanceof ServerPlayer server?RpgAbilities.profile(server).rpgClass()==RpgClass.DEUS_TROVAO:p.isLocalPlayer()&&EquipmentAccess.clientSelected()==RpgClass.DEUS_TROVAO);}
    public static void waterMovement(net.minecraft.world.entity.player.Player p){if(cannotSwim(p)){var v=p.getDeltaMovement();p.setDeltaMovement(v.x*.85,Math.min(-.06,v.y),v.z*.85);}}
    public static boolean handles(RpgClass c){return c==RpgClass.ARQUEIRO_NOVO||c==RpgClass.DEUS_TROVAO;}
    private static State state(ServerPlayer p){var c=RpgAbilities.profile(p).rpgClass();var s=STATES.get(p.getUUID());if(s==null||s.type!=c||s.owner!=p){clear(p);s=new State(p,c);STATES.put(p.getUUID(),s);}return s;}
    private static long now(ServerPlayer p){return p.level().getGameTime();}
    public static boolean freeRoll(ServerPlayer p){var a=STATES.get(p.getUUID());return RpgAbilities.profile(p).rpgClass()==RpgClass.ARQUEIRO_NOVO&&a!=null&&a.until[2]>now(p);}
    private static boolean guidedAvailable(ServerPlayer p){var a=STATES.get(p.getUUID());return freeRoll(p)&&a.cast[2]!=null&&a.cast[2].mainUnlocked(2);}
    public static boolean guided(DamageSource source){return source.getDirectEntity() instanceof AbstractArrow a&&a.getPersistentData().getBooleanOr("oxy_guided",false);}
    public static void mark(ServerPlayer p,int id){
        if(RpgAbilities.profile(p).rpgClass()!=RpgClass.ARQUEIRO_NOVO){MARKS.remove(p.getUUID());return;}
        var entity=p.level().getEntity(id);
        if(entity instanceof Mob m&&RpgAbilities.attackable(m)&&p.distanceToSqr(m)<=64*64&&p.hasLineOfSight(m))MARKS.put(p.getUUID(),new Mark(m,now(p)+20));
        else MARKS.remove(p.getUUID());
    }
    private static Mob target(ServerPlayer p){var mark=MARKS.get(p.getUUID());if(mark!=null&&mark.until>now(p)&&mark.mob.level()==p.level()&&mark.mob.isAlive()&&p.distanceToSqr(mark.mob)<=64*64&&p.hasLineOfSight(mark.mob))return mark.mob;return AbilityAim.target(p);}
    private static int count(RpgProfile s,int slot){return slot==0?(s.mainUnlocked(0)?6:2)+(s.hasSkill(4)?2:0)+(s.hasSkill(6)?2:0):(s.mainUnlocked(1)?10:4)+(s.hasSkill(11)?4:0);}
    private static int ammo(ServerPlayer p){int n=0;for(int i=0;i<36;i++){var item=p.getInventory().getItem(i);if(item.is(ItemTags.ARROWS))n+=item.getCount();}if(p.getOffhandItem().is(ItemTags.ARROWS))n+=p.getOffhandItem().getCount();return n;}
    private static void consume(ServerPlayer p,int amount){if(p.hasInfiniteMaterials()||RpgDebug.unlimited(p))return;for(int i=0;i<36&&amount>0;i++){var item=p.getInventory().getItem(i);if(item.is(ItemTags.ARROWS)){int n=Math.min(amount,item.getCount());item.shrink(n);amount-=n;}}if(amount>0)p.getOffhandItem().shrink(amount);p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();}
    private static Vec3 teleportPoint(ServerPlayer p,RpgProfile s){return teleportDestination(p,s,AbilityAim.point(p,teleportRange(s)));}
    public static double teleportRange(RpgProfile s){return (s.mainUnlocked(0)?12:5)+(s.hasSkill(4)?4:0)+(s.hasSkill(6)?4:0);}
    /** Shared by the preview and server execution: identical range, clip and clearance. */
    public static Vec3 teleportDestination(net.minecraft.world.entity.player.Player p,RpgProfile s,Vec3 aim){
        double range=teleportRange(s);var direction=aim.subtract(p.getEyePosition()).normalize();
        var end=p.getEyePosition().add(direction.scale(Math.min(range,aim.distanceTo(p.getEyePosition()))));
        var point=p.level().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p)).getLocation().subtract(direction.scale(.7));
        // Search only the destination and its headroom, never generate distant chunks.
        for(int y=0;y<=2;y++){var at=point.add(0,y,0);if(p.level().hasChunkAt(BlockPos.containing(at))&&p.level().noCollision(p,p.getBoundingBox().move(at.subtract(p.position()))))return at;}
        return null;
    }
    public static void validate(ServerPlayer p,RpgProfile s,int slot){
        if(!handles(s.rpgClass()))return;
        if(s.rpgClass()==RpgClass.ARQUEIRO_NOVO&&slot<2){
            if(!p.hasInfiniteMaterials()&&!RpgDebug.unlimited(p)&&ammo(p)<count(s,slot))throw new IllegalStateException("Precisa de "+count(s,slot)+" flechas. Mana não foi gasta.");
            if(slot==1&&target(p)==null)throw new IllegalStateException("Marque uma criatura visível para a chuva de flechas.");
        }
        if(s.rpgClass()==RpgClass.DEUS_TROVAO&&slot==0&&teleportPoint(p,s)==null)throw new IllegalStateException("Não há espaço seguro no destino do teleporte.");
    }
    public static void activate(ServerPlayer p,RpgProfile s,int slot){
        var a=state(p);a.cast[slot]=s;a.until[slot]=now(p)+s.abilityDuration(slot);
        if(s.rpgClass()==RpgClass.ARQUEIRO_NOVO){
            if(slot==0){consume(p,count(s,0));a.burst=count(s,0);a.shot=0;}
            else if(slot==1){var target=target(p);consume(p,count(s,1));for(int i=0;i<count(s,1);i++)arrow(p,s,1,target,i);}
            else{p.addEffect(new MobEffectInstance(MobEffects.SPEED,s.abilityDuration(2),s.mainUnlocked(2)?1:0));p.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST,s.abilityDuration(2),s.hasSkill(20)?4:3));}
        }else{
            if(slot==0){var at=teleportPoint(p,s);var start=p.position();RpgEvasion.cancelMotion(p);p.teleportTo(at.x,at.y,at.z);p.resetFallDistance();lightning(p,at);chain(p,s,at,s.mainUnlocked(0)?3+(s.hasSkill(5)?1:0):1.5);ClassJourney.advance(p,0,1);arc(p,start.add(0,1,0),at.add(0,1,0));}
            else if(slot==1)channel(p,s);
            else sound(p,SoundEvents.LIGHTNING_BOLT_THUNDER,.35f,1.6f);
        }
        sync(p);
    }
    private static void arrow(ServerPlayer p,RpgProfile s,int mode,Mob target,int index){
        var a=state(p);if(mode==0&&guidedAvailable(p))target=target(p);var shot=new Arrow(p.level(),p,new ItemStack(Items.ARROW),null);shot.pickup=AbstractArrow.Pickup.DISALLOWED;
        shot.setBaseDamage(mode==0?(s.mainUnlocked(0)?2.2:1.6)*(s.hasSkill(5)?1.15:1):(s.mainUnlocked(1)?2.6:1.8)*(s.hasSkill(9)?1.15:1)*(s.hasSkill(12)?1.25:1));
        if(mode==0){var dir=AbilityAim.direction(p).yRot((index%3-1)*.035f);shot.shoot(dir.x,dir.y,dir.z,2.6f,0);}
        else {double theta=index*Math.PI*2/count(s,1);shot.shoot(Math.cos(theta)*.22,1,Math.sin(theta)*.22,1.7f,0);}
        shot.getPersistentData().putInt("oxy_archer_mode",mode+1);if(mode==0&&target!=null)shot.getPersistentData().putBoolean("oxy_guided",true);
        var f=new Flight(shot,p,s,target,mode);ARROWS.put(shot.getUUID(),f);p.level().addFreshEntity(shot);
        sound(p,SoundEvents.ARROW_SHOOT,.4f,1.2f);
    }
    public static void join(EntityJoinLevelEvent e){
        if(e.getLevel().isClientSide()||!(e.getEntity() instanceof AbstractArrow arrow)||!(arrow.getOwner() instanceof ServerPlayer p)||!guidedAvailable(p)||arrow.getPersistentData().contains("oxy_archer_mode"))return;
        var target=target(p);if(target==null)return;arrow.getPersistentData().putBoolean("oxy_guided",true);
        arrow.setNoGravity(true);arrow.setDeltaMovement(arrow.getDeltaMovement().scale(1.4));
        ARROWS.put(arrow.getUUID(),new Flight(arrow,p,RpgAbilities.profile(p),target,2));
    }
    public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        var s=RpgAbilities.profile(p);var old=STATES.get(p.getUUID());
        if(!p.isAlive()||!handles(s.rpgClass())){if(old!=null)clear(p);return;}var a=state(p);
        if(s.rpgClass()==RpgClass.ARQUEIRO_NOVO){if(a.burst>0&&p.tickCount%2==0){arrow(p,a.cast[0],0,null,a.shot++);a.burst--;}}
        else{
            if(a.until[1]>now(p)&&p.tickCount%5==0)channel(p,a.cast[1]);
            boolean storm=a.until[2]>now(p);
            Professions.modifier(p,Attributes.MOVEMENT_SPEED,"thunder_speed",storm?(s.mainUnlocked(2)?1.8:.9)+(s.hasSkill(19)?.5:0):0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            Professions.modifier(p,Attributes.STEP_HEIGHT,"thunder_step",storm?.6+(s.hasSkill(20)?.5:0):0,AttributeModifier.Operation.ADD_VALUE);
            Professions.modifier(p,Attributes.JUMP_STRENGTH,"thunder_jump",storm?(s.mainUnlocked(2)?.5:.3)+(s.hasSkill(20)?.1:0):0,AttributeModifier.Operation.ADD_VALUE);
            if(p.isSprinting()&&!p.isInWater()&&!p.isInLava()&&!p.getAbilities().flying&&!p.isFallFlying())a.running=Math.min(160,a.running+1);else a.running=0;
            Professions.modifier(p,Attributes.MOVEMENT_SPEED,"thunder_running",a.running>=60?.75+(a.running-60)*.005:0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            if(!p.hasInfiniteMaterials()){
                
                if(p.tickCount%20==0){boolean hot=!p.isInWater()&&(p.level().dimensionType().ultraWarm()||p.level().getBiome(p.blockPosition()).value().getBaseTemperature()>=1.5f);a.heat=hot?Math.min(12,a.heat+1):Math.max(0,a.heat-2);
                    if(a.heat>=10){if(!a.overheated)Professions.notify(p,"Superaquecimento: procure um ambiente fresco.");a.overheated=true;p.hurtServer(p.level(),p.damageSources().dryOut(),1);}else a.overheated=false;}
            }
        }
        if(p.tickCount%10==0)sync(p);
        for(var it=ARROWS.values().iterator();it.hasNext();){var f=it.next();if(f.owner!=p)continue;var shot=f.arrow;
            if(shot.isRemoved()||!shot.isAlive()||now(p)-f.birth>160||shot.level()!=p.level()){if(!shot.isRemoved()){shot.getPersistentData().remove("oxy_guided");shot.setNoGravity(false);if(f.mode!=2)shot.discard();}it.remove();continue;}
            boolean follow=f.mode==2||f.mode==0&&shot.getPersistentData().getBooleanOr("oxy_guided",false)||f.mode==1&&now(p)-f.birth>=12;
            if(follow&&f.target!=null&&f.target.isAlive()&&f.target.level()==shot.level()){
                var goal=f.target.getBoundingBox().getCenter();var direction=goal.subtract(shot.position());
                // Resolve a swept approach to tiny moving targets (Vex) through native
                // impact handling, retaining shields, events, damage and debuffs.
                double speed=f.mode==2?(f.profile.hasSkill(19)?4:3.4):2.3;
                var next=shot.position().add(direction.normalize().scale(Math.min(speed,direction.length())));
                var box=f.target.getBoundingBox().inflate(.12);
                var contact=box.contains(shot.position())?shot.position():box.clip(shot.position(),next).orElse(null);
                if(contact!=null&&p.level().clip(new net.minecraft.world.level.ClipContext(shot.position(),contact,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,shot)).getType()==HitResult.Type.MISS){
                    var hit=new EntityHitResult(f.target,contact);
                    if(!net.neoforged.neoforge.event.EventHooks.onProjectileImpact(shot,hit)){
                        ((dev.emersonoxy.rpg.mixin.GuidedArrowInvoker)shot).oxyGuidedHit(hit);
                        if(shot.isRemoved()){it.remove();continue;}
                    }
                }
                shot.setNoGravity(true);shot.setDeltaMovement(direction.normalize().scale(Math.min(speed,direction.length()+.1)));shot.setYRot((float)Math.toDegrees(Math.atan2(direction.x,direction.z)));shot.setXRot((float)Math.toDegrees(Math.atan2(direction.y,direction.horizontalDistance())));shot.hasImpulse=true;
                // Arrow clients extrapolate velocity, so explicitly update homing turns.
                if(p.tickCount%2==0)p.level().getChunkSource().sendToTrackingPlayers(shot,new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(shot));
            }
        }
    }
    public static void impact(ProjectileImpactEvent event){
        var f=ARROWS.get(event.getProjectile().getUUID());if(f==null||event.isCanceled()||!(event.getProjectile().level() instanceof ServerLevel level))return;
        if(f.mode==1&&f.profile.hasSkill(13)&&!f.exploded){f.exploded=true;var point=event.getRayTraceResult().getLocation();level.sendParticles(ParticleTypes.EXPLOSION,point.x,point.y,point.z,1,0,0,0,0);sound(f.owner,SoundEvents.GENERIC_EXPLODE.value(),.5f,1.5f);
            for(var mob:level.getEntitiesOfClass(Mob.class,new AABB(point,point).inflate(2.5),RpgAbilities::attackable))if(mob.position().distanceToSqr(point)<6.25&&f.owner.hasLineOfSight(mob))mob.hurtServer(level,f.owner.damageSources().indirectMagic(f.arrow,f.owner),3+(f.profile.hasSkill(12)?1:0));}
        // Native arrow collision, shields, damage and pickup rules still run.
    }
    public static void hit(LivingDamageEvent.Post event){
        if(event.getNewDamage()<=0||Bleeding.damage(event.getSource()))return;
        if(event.getSource().getDirectEntity() instanceof AbstractArrow shot){var f=ARROWS.get(shot.getUUID());if(f!=null){var target=event.getEntity();
            if(f.mode<2)ClassJourney.advance(f.owner,f.mode,1);
            if(f.mode==0){target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,f.profile.hasSkill(2)?100:60,0));if(f.profile.mainUnlocked(0))target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,80,0));if(f.profile.hasSkill(6))target.addEffect(new MobEffectInstance(MobEffects.POISON,60,0));}
            if(f.mode==2||shot.getPersistentData().getBooleanOr("oxy_guided",false))target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,f.profile.hasSkill(18)?120:80,f.profile.hasSkill(20)?1:0));
        }}
        if(!(event.getSource().getEntity() instanceof ServerPlayer p))return;var s=RpgAbilities.profile(p);
        if(s.rpgClass()!=RpgClass.DEUS_TROVAO||event.getSource().getDirectEntity()!=p||!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)||!p.getMainHandItem().isEmpty()||!p.getOffhandItem().isEmpty())return;
        var a=STATES.get(p.getUUID());if(a==null||a.until[2]<=now(p))return;var victim=event.getEntity();victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40,0));
        ClassJourney.advance(p,2,1);arc(p,p.getEyePosition(),victim.getBoundingBox().getCenter());chainFrom(p,a.cast[2],victim,a.cast[2].mainUnlocked(2)?3:1,2);
    }
    public static void damage(LivingDamageEvent.Pre e){if(e.getSource().getEntity() instanceof ServerPlayer p&&RpgAbilities.profile(p).rpgClass()==RpgClass.DEUS_TROVAO&&e.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)&&p.getMainHandItem().isEmpty()&&p.getOffhandItem().isEmpty()){var a=STATES.get(p.getUUID());if(a!=null&&a.until[2]>now(p))e.setNewDamage(e.getNewDamage()+2);}if(e.getSource().getEntity() instanceof ServerPlayer p&&RpgAbilities.profile(p).rpgClass()==RpgClass.ARQUEIRO_NOVO&&e.getSource().getDirectEntity() instanceof AbstractArrow)e.setNewDamage(e.getNewDamage()*1.05f);}
    private static void channel(ServerPlayer p,RpgProfile s){var target=AbilityAim.target(p);var end=target==null?AbilityAim.point(p,s.hasSkill(11)?24:18):target.getBoundingBox().getCenter();arc(p,p.getEyePosition(),end);
        if(target!=null&&p.distanceToSqr(target)<(s.hasSkill(11)?24*24:18*18)&&p.tickCount%10==0){target.hurtServer(p.level(),new DamageSource(p.damageSources().lightningBolt().typeHolder(),p,p),s.mainUnlocked(1)?3+(s.hasSkill(9)?1:0):1.5f);ClassJourney.advance(p,1,1);chainFrom(p,s,target,s.mainUnlocked(1)?2+(s.hasSkill(13)?2:0):0,1.5f);}
        if(p.tickCount%20==0)sound(p,SoundEvents.LIGHTNING_BOLT_IMPACT,.25f,1.7f);
    }
    private static void chain(ServerPlayer p,RpgProfile s,Vec3 at,double radius){for(var mob:p.level().getEntitiesOfClass(Mob.class,new AABB(at,at).inflate(radius),RpgAbilities::attackable))if(mob.position().distanceToSqr(at)<=radius*radius&&p.hasLineOfSight(mob)){mob.hurtServer(p.level(),new DamageSource(p.damageSources().lightningBolt().typeHolder(),p,p),s.mainUnlocked(0)?6+(s.hasSkill(5)?2:0):3);if(s.hasSkill(2))mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40,0));}}
    private static void chainFrom(ServerPlayer p,RpgProfile s,LivingEntity first,int jumps,float damage){
        var visited=new HashSet<UUID>();visited.add(first.getUUID());LivingEntity previous=first;double radius=s.hasSkill(6)?6:4;
        for(int i=0;i<jumps;i++){var from=previous;var next=p.level().getEntitiesOfClass(Mob.class,from.getBoundingBox().inflate(radius),m->RpgAbilities.attackable(m)&&!visited.contains(m.getUUID())&&from.distanceToSqr(m)<=radius*radius&&from.hasLineOfSight(m)).stream().min(Comparator.comparingDouble(from::distanceToSqr)).orElse(null);if(next==null)break;visited.add(next.getUUID());arc(p,from.getBoundingBox().getCenter(),next.getBoundingBox().getCenter());next.hurtServer(p.level(),new DamageSource(p.damageSources().lightningBolt().typeHolder(),p,p),damage*(s.hasSkill(5)?1.2f:1));next.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,30,0));previous=next;}
    }
    private static void arc(ServerPlayer p,Vec3 from,Vec3 to){
        var center=from.lerp(to,.5);PacketDistributor.sendToPlayersNear(p.level(),null,center.x,center.y,center.z,48,new RpgNetwork.ElectricArc(from.x,from.y,from.z,to.x,to.y,to.z));
    }
    private static void lightning(ServerPlayer p,Vec3 at){var bolt=EntityType.LIGHTNING_BOLT.create(p.level(),EntitySpawnReason.TRIGGERED);if(bolt!=null){bolt.setPos(at);bolt.setVisualOnly(true);p.level().addFreshEntity(bolt);}}
    private static void sound(ServerPlayer p,SoundEvent sound,float volume,float pitch){p.level().playSound(null,p.blockPosition(),sound,SoundSource.PLAYERS,volume,pitch);}
    public static void sync(ServerPlayer p){var a=STATES.get(p.getUUID());int[] left=new int[3],total=new int[3];for(int i=0;i<3;i++){left[i]=a==null?0:(int)Math.max(0,a.until[i]-now(p));total[i]=a==null||a.cast[i]==null?1:a.cast[i].abilityDuration(i);}PacketDistributor.sendToPlayer(p,new RpgNetwork.AbilityStatus(left[0],left[1],left[2],total[0],total[1],total[2],left[0]>0?3:0,left[1]>0?3:0,left[2]>0?3:0));}
    public static void clear(ServerPlayer p){STATES.remove(p.getUUID());MARKS.remove(p.getUUID());for(var f:ARROWS.values())if(f.owner.getUUID().equals(p.getUUID())){f.arrow.getPersistentData().remove("oxy_guided");f.arrow.setNoGravity(false);}ARROWS.entrySet().removeIf(e->e.getValue().owner.getUUID().equals(p.getUUID()));for(var attribute:List.of(Attributes.MOVEMENT_SPEED,Attributes.STEP_HEIGHT,Attributes.JUMP_STRENGTH))for(String key:List.of("thunder_speed","thunder_step","thunder_running","thunder_jump"))Professions.modifier(p,attribute,key,0,AttributeModifier.Operation.ADD_VALUE);}
    public static void stop(ServerStoppedEvent e){STATES.clear();MARKS.clear();ARROWS.clear();}
}
