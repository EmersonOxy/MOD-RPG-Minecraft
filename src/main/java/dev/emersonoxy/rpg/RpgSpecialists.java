package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraft.tags.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Bounded, server-owned casts. Temporary creatures never produce loot or XP. */
public final class RpgSpecialists {
    private static final String TEMP="oxy_special_temporary";
    private static final Map<UUID,Cast> CASTS=new HashMap<>();
    private static final Map<UUID,Shot> SHOTS=new HashMap<>();
    private static final Map<UUID,ServerPlayer> OWNERS=new HashMap<>();
    private static final class Cast {
        final ServerPlayer player;final ServerLevel world;RpgProfile profile;
        final long[] until=new long[3];final List<Mob> copies=new ArrayList<>(),bats=new ArrayList<>(),sea=new ArrayList<>();
        final List<Core> cores=new ArrayList<>();final LinkedHashMap<UUID,Integer> fireworks=new LinkedHashMap<>();final List<Line> lines=new ArrayList<>();final List<Vec3> embers=new ArrayList<>();
        Mob disguise,levitated;Vec3 safe,jet;long nextExplosion,nextMagma,nextBoost,overheated,heated,fireLanding;int chain;float heat;boolean ending,ground,launchAir;
        Cast(ServerPlayer p,RpgProfile s){player=p;world=p.level();profile=s;safe=p.position();heat=RpgDebug.unlimited(p)?100:0;}
    }
    private static final class Core {Vec3 position;UUID target;long expires;Core(Vec3 p,UUID target,long expires){position=p;this.target=target;this.expires=expires;}}
    private record Line(Vec3 a,Vec3 b,long expires){}
    private record Shot(ServerPlayer player,RpgProfile profile,long expires){}
    private RpgSpecialists(){}
    public static boolean handles(RpgClass c){return c==RpgClass.ILUSIONISTA||c==RpgClass.HIDROMANTE||c==RpgClass.PIROMANTE;}
    public static boolean temporary(Entity e){return e!=null&&e.getPersistentData().getBooleanOr(TEMP,false);}
    public static boolean intangible(Player p){return p.getPersistentData().getLongOr("oxy_eclipse",0)>p.level().getGameTime();}
    public static boolean disguised(Player p){var c=CASTS.get(p.getUUID());return c!=null&&c.player==p&&c.world==p.level()&&c.profile.rpgClass()==RpgClass.ILUSIONISTA&&c.disguise!=null&&c.disguise.isAlive()&&c.until[0]>p.level().getGameTime();}
    /** Shared prediction uses ordinary terrain collision, independent of ghost collision shapes. */
    public static void ghostFlight(Player p,boolean up,boolean down){
        p.setNoGravity(true);p.fallDistance=0;double height=phaseTier(p)>=3?8:5;
        var at=p.position();var ground=p.level().clip(new ClipContext(at.add(0,.1,0),at.add(0,-height-.3,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty()));
        double vertical=down?-.22:up?.22:0;if(ground.getType()==HitResult.Type.MISS)vertical=Math.min(vertical,-.16);else if(at.y-ground.getLocation().y>=height)vertical=Math.min(vertical,0);
        var motion=p.getDeltaMovement();double cap=phaseTier(p)>=3?.55:.4,h=motion.horizontalDistance();p.setDeltaMovement(h>cap?new Vec3(motion.x*cap/h,vertical,motion.z*cap/h):new Vec3(motion.x,vertical,motion.z));
    }
    public static boolean waterWalking(Player p){return !p.isShiftKeyDown()&&p.getPersistentData().getLongOr("oxy_waterwalk",0)>p.level().getGameTime();}
    public static int phaseTier(Player p){return p.getPersistentData().getIntOr("oxy_phase_tier",0);}
    public static boolean extra(ServerPlayer p,int slot){var c=CASTS.get(p.getUUID());if(c==null||c.profile.rpgClass()!=RpgClass.PIROMANTE)return false;return slot==0&&!c.fireworks.isEmpty()||slot==1&&!c.cores.isEmpty();}
    public static void validate(ServerPlayer p,RpgProfile s,int slot){
        if(!p.isAlive()||p.isSpectator())throw new IllegalStateException("Habilidades exigem um jogador vivo fora do modo espectador.");
        if(intangible(p))throw new IllegalStateException("O enxame não pode atacar nem usar outras habilidades.");
        var c=CASTS.get(p.getUUID());long now=p.level().getGameTime();
        if(c!=null&&c.overheated>now&&!RpgDebug.unlimited(p)&&!RpgDebug.cooldownsDisabled(p))throw new IllegalStateException("Caldeira superaquecida: aguarde esfriar.");
        if(s.rpgClass()==RpgClass.ILUSIONISTA&&slot<2){var target=AbilityAim.target(p);if(target==null)throw new IllegalStateException("Mire em um mob visível a até 24 blocos.");if(slot==0&&(target.getType()==EntityType.WITHER||target.getType()==EntityType.ENDER_DRAGON||target.getType()==EntityType.WARDEN||target.getBbWidth()>3||target.getBbHeight()>4))throw new IllegalStateException("Essa criatura é poderosa demais para o Mímico.");}
        if(s.rpgClass()==RpgClass.HIDROMANTE&&slot==0&&!p.isInWater())throw new IllegalStateException("O impulso exige estar dentro da água; os benefícios aquáticos são passivos.");
        if(s.rpgClass()==RpgClass.PIROMANTE&&slot==2&&(c==null||c.heat<30)&&!RpgDebug.unlimited(p))throw new IllegalStateException("A Fênix precisa de 30 de calor.");
        if(c!=null&&(slot==0&&extra(p,slot)&&now<c.nextExplosion||slot==1&&now<c.nextMagma))throw new IllegalStateException("Aguarde a detonação em andamento.");
    }
    private static Cast cast(ServerPlayer p,RpgProfile s){var c=CASTS.get(p.getUUID());if(c==null||c.world!=p.level()||c.profile.rpgClass()!=s.rpgClass()){clear(p);c=new Cast(p,s);CASTS.put(p.getUUID(),c);}c.profile=s;return c;}
    public static void activate(ServerPlayer p,RpgProfile s,int slot){
        var c=cast(p,s);long now=c.world.getGameTime();
        if(s.rpgClass()==RpgClass.PIROMANTE&&extra(p,slot)){
            if(slot==0){c.nextExplosion=now+1;c.chain=0;ClassJourney.advance(p,0,1);}else if(p.isShiftKeyDown()){detonate(c,false);ClassJourney.advance(p,1,1);}else{launchMagma(c,now);}sync(p);return;
        }
        c.until[slot]=now+s.abilityDuration(slot);
        switch(s.rpgClass()){
            case ILUSIONISTA -> {
                if(slot==0){
                    discard(c.copies);if(c.disguise!=null){OWNERS.remove(c.disguise.getUUID());c.disguise.discard();}var target=AbilityAim.target(p);
                    int count=2+(s.hasSkill(4)?1:0)+(s.hasSkill(6)?1:0);
                    for(int i=0;i<count;i++){var mob=spawn(c,target.getType(),p.position().add(Math.cos(i*6.28/count)*2,0,Math.sin(i*6.28/count)*2));if(mob!=null){mob.setHealth(Math.min(mob.getMaxHealth(),s.hasSkill(2)?6:3));c.copies.add(mob);}}
                    c.disguise=spawn(c,target.getType(),p.position(),true);if(c.disguise!=null){c.disguise.setInvulnerable(true);c.disguise.setNoGravity(true);c.disguise.noPhysics=true;}
                    if(s.hasSkill(5))c.until[0]+=60;
                    ghost(p,(int)(c.until[0]-now));for(var enemy:near(c,p.position(),32))if(enemy.getTarget()==p)enemy.setTarget(null);ClassJourney.advance(p,0,1);
                }else if(slot==1){c.levitated=AbilityAim.target(p);effect(c.levitated,MobEffects.LEVITATION,s.abilityDuration(slot)+(s.hasSkill(12)?40:0),0);effect(c.levitated,MobEffects.WEAKNESS,s.abilityDuration(slot),s.mainUnlocked(slot)?1:0);ClassJourney.advance(p,1,1);}
                else{p.closeContainer();c.safe=p.position();p.getPersistentData().putLong("oxy_eclipse",c.until[2]);p.getPersistentData().putInt("oxy_phase_tier",s.hasSkill(20)?3:s.hasSkill(19)?2:1);ghost(p,(int)(c.until[2]-now));
                    for(int i=0;i<6;i++){var bat=spawn(c,EntityType.BAT,p.position().add(0,1,0),true);if(bat!=null){bat.setInvulnerable(true);bat.setNoGravity(true);if(bat instanceof net.minecraft.world.entity.ambient.Bat b)b.setResting(false);bat.noPhysics=true;c.bats.add(bat);}}ClassJourney.advance(p,2,1);
                }
            }
            case HIDROMANTE -> {
                if(slot==0){double speed=(s.mainUnlocked(0)?1.0:.6)+(s.hasSkill(4)?.25:0)+s.level()*.003;Vec3 direction=AbilityAim.direction(p);p.setDeltaMovement(direction.scale(speed).add(0,direction.y>.3?(s.hasSkill(5)?.8:.45):.1,0));p.hurtMarked=true;ClassJourney.advance(p,0,1);}
                else if(slot==1){c.jet=AbilityAim.direction(p);if(p.isUnderWater()){discard(c.sea);var types=List.of(EntityType.COD,EntityType.SALMON,EntityType.TROPICAL_FISH,EntityType.SQUID,EntityType.DOLPHIN);for(int i=0;i<(s.hasSkill(13)?3:2);i++){var mob=spawn(c,types.get(p.getRandom().nextInt(types.size())),p.position().add(i-1,0,1));if(mob!=null)c.sea.add(mob);}}}
                else{c.until[2]+=s.hasSkill(19)?100:0;p.getPersistentData().putLong("oxy_waterwalk",c.until[2]);if(p.isInWater()){float before=p.getHealth();p.heal((s.mainUnlocked(2)?4:2)+(s.hasSkill(16)?2:0)+(s.hasSkill(20)?2:0));if(p.getHealth()>before)ClassJourney.advance(p,2,1);if(s.hasSkill(20)){p.removeEffect(MobEffects.POISON);p.removeEffect(MobEffects.WEAKNESS);}if(s.mainUnlocked(2))FluidPockets.drain(p,p.blockPosition(),s.hasSkill(18)?2:1,100);}}
            }
            case PIROMANTE -> {
                if(slot==0){c.fireworks.clear();c.nextExplosion=0;for(var mob:near(c,p.position(),s.hasSkill(4)?7:5)){c.fireworks.put(mob.getUUID(),mob.isOnFire()&&s.mainUnlocked(0)?2:1);if(c.fireworks.size()>=(s.hasSkill(4)?8:5))break;}if(c.fireworks.isEmpty())c.until[0]=0;heat(c,8);}
                else if(slot==1){launchMagma(c,now);}
                else{c.heat=Math.max(0,c.heat-30);c.fireLanding=now+s.abilityDuration(2)+200;c.launchAir=false;p.fallDistance=0;p.setDeltaMovement(p.getDeltaMovement().add(0,.7,0));p.hurtMarked=true;ClassJourney.advance(p,2,1);}
            }
            default -> {}
        }
        c.world.playSound(null,p.blockPosition(),s.rpgClass()==RpgClass.ILUSIONISTA?net.minecraft.sounds.SoundEvents.ILLUSIONER_CAST_SPELL:s.rpgClass()==RpgClass.HIDROMANTE?net.minecraft.sounds.SoundEvents.PLAYER_SPLASH:net.minecraft.sounds.SoundEvents.BLAZE_SHOOT,net.minecraft.sounds.SoundSource.PLAYERS,.65f,1.1f);
        sync(p);RpgAbilities.syncStatus(p);
    }
    private static void launchMagma(Cast c,long now){var p=c.player;var shot=new Snowball(c.world,p,new ItemStack(Items.MAGMA_CREAM));shot.setNoGravity(true);shot.getPersistentData().putBoolean("oxy_magma",true);SHOTS.put(shot.getUUID(),new Shot(p,c.profile,now+100));AbilityAim.shoot(shot,p,1.2f);c.world.addFreshEntity(shot);c.nextMagma=now+10;c.until[1]=now+240;heat(c,8);}
    private static List<Mob> near(Cast c,Vec3 at,double radius){return c.world.getEntitiesOfClass(Mob.class,new AABB(at,at).inflate(radius),m->RpgAbilities.attackable(m)&&m.distanceToSqr(at)<=radius*radius);}
    private static void effect(LivingEntity e,net.minecraft.core.Holder<MobEffect> type,int time,int level){e.addEffect(new MobEffectInstance(type,time,level,false,false,true));}
    private static Mob spawn(Cast c,EntityType<?> type,Vec3 at){return spawn(c,type,at,false);}
    private static Mob spawn(Cast c,EntityType<?> type,Vec3 at,boolean cosmetic){
        if(type==EntityType.ENDER_DRAGON||type==EntityType.WITHER||type==EntityType.WARDEN||type==EntityType.PLAYER)return null;
        var entity=type.create(c.world,EntitySpawnReason.TRIGGERED);if(!(entity instanceof Mob mob))return null;
        if(mob.getBbWidth()>3||mob.getBbHeight()>4)return null;
        mob.setPos(at);if(!c.world.noCollision(mob))return null;
        mob.setNoAi(true);mob.setPersistenceRequired();mob.getPersistentData().putBoolean(TEMP,true);mob.getPersistentData().putBoolean("oxy_cosmetic",cosmetic);OWNERS.put(mob.getUUID(),c.player);c.world.addFreshEntity(mob);return mob;
    }
    private static void ghost(ServerPlayer p,int ticks){PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new RpgNetwork.Ghost(p.getId(),ticks));}
    public static int remaining(ServerPlayer p,int slot){var c=CASTS.get(p.getUUID());return c==null?0:(int)Math.max(0,c.until[slot]-p.level().getGameTime());}
    public static void sync(ServerPlayer p){var c=CASTS.get(p.getUUID());int phase=intangible(p)?phaseTier(p):0;PacketDistributor.sendToPlayer(p,new RpgNetwork.SpecialState(phase,remaining(p,2),c==null?0:c.heat,c==null?0:(int)Math.max(0,c.overheated-p.level().getGameTime()),waterWalking(p)));}
    private static void heat(Cast c,float amount){if(RpgDebug.unlimited(c.player)){c.heat=100;c.overheated=0;return;}if(c.overheated>c.world.getGameTime()&&!RpgDebug.cooldownsDisabled(c.player))return;c.heat=Math.clamp(c.heat+amount,0,100);c.heated=c.world.getGameTime();if(c.heat>=100){c.heat=0;c.overheated=RpgDebug.cooldownsDisabled(c.player)?0:c.world.getGameTime()+120;if(!RpgDebug.cooldownsDisabled(c.player))c.player.displayClientMessage(net.minecraft.network.chat.Component.literal("[Oxy RPG] Superaquecimento! Caldeira em recuperação por 6 s."),true);}}
    private static float power(Cast c,float damage){return damage*(.65f+.35f*c.profile.level()/100f);}
    private static void hit(Cast c,Mob mob,float damage){String name=c.profile.rpgClass()==RpgClass.PIROMANTE?"ember":c.profile.rpgClass()==RpgClass.HIDROMANTE?"pressure":"illusion";var key=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("oxyrpg",name));var type=c.world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getOrThrow(key);mob.hurtServer(c.world,new net.minecraft.world.damagesource.DamageSource(type,null,c.player),power(c,damage));}
    private static void explosion(Cast c,Vec3 at,double radius,float damage,boolean launch){
        for(var mob:near(c,at,radius)){if(!c.world.clip(new ClipContext(at.add(0,.2,0),mob.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,c.player)).getType().equals(HitResult.Type.MISS))continue;hit(c,mob,damage);if(launch)mob.push(0,c.profile.hasSkill(5)?.65:.3,0);mob.igniteForSeconds(3);}
        SkillEffects.impact(c.world,at,radius,1);
        c.world.sendParticles(ParticleTypes.EXPLOSION,at.x,at.y+.5,at.z,3,.3,.3,.3,0);
        c.world.playSound(null,BlockPos.containing(at),net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),net.minecraft.sounds.SoundSource.PLAYERS,.55f,1.3f);
    }
    private static void detonate(Cast c,boolean finish){
        long now=c.world.getGameTime();int count=Math.min(8,c.cores.size());
        for(int i=0;i<count;i++){var a=c.cores.get(i);explosion(c,a.position,2.5,c.profile.hasSkill(13)?8:6,false);PyroTerrain.impact(c.player,a.position,c.profile.mainUnlocked(1));if(c.profile.mainUnlocked(1))for(int j=i+1;j<count;j++){var b=c.cores.get(j);if(a.position.distanceToSqr(b.position)<=100)c.lines.add(new Line(a.position,b.position,now+(c.profile.hasSkill(12)?100:60)));}}
        c.cores.clear();c.nextMagma=now+10;if(!finish)heat(c,5);
    }
    public static void projectile(net.neoforged.neoforge.event.entity.ProjectileImpactEvent e){
        var shot=e.getEntity();var data=SHOTS.remove(shot.getUUID());if(data==null)return;
        var c=CASTS.get(data.player().getUUID());if(c!=null&&c.world==shot.level()){var impact=e.getRayTraceResult().getLocation();explosion(c,impact,c.profile.mainUnlocked(1)?3:2,c.profile.mainUnlocked(1)?6:4,false);PyroTerrain.impact(c.player,impact,c.profile.mainUnlocked(1));int max=c.profile.hasSkill(11)?6:3;if(c.cores.size()>=max)c.cores.remove(0);UUID target=e.getRayTraceResult() instanceof EntityHitResult hit&&hit.getEntity() instanceof Mob mob&&RpgAbilities.attackable(mob)?mob.getUUID():null;var point=e.getRayTraceResult().getLocation();c.cores.add(new Core(point,target,c.world.getGameTime()+240));if(target!=null){var mob=(Mob)c.world.getEntity(target);hit(c,mob,2);mob.igniteForSeconds(c.profile.hasSkill(9)?5:3);}SkillEffects.impact(c.world,point,1.2,1);sync(c.player);}
        shot.discard();e.setCanceled(true);
    }
    public static void tick(EntityTickEvent.Post e){
        var entity=e.getEntity();if(!(entity.level() instanceof ServerLevel))return;
        if(!(entity instanceof ServerPlayer)&&!(entity instanceof Snowball&&SHOTS.containsKey(entity.getUUID())))return;
        RpgProfiler.run("specialists",()->tickBody(e));
    }
    private static void tickBody(EntityTickEvent.Post e){
        if(e.getEntity() instanceof Snowball shot&&SHOTS.containsKey(shot.getUUID())){var data=SHOTS.get(shot.getUUID());if(shot.level().getGameTime()>=data.expires()){SHOTS.remove(shot.getUUID());shot.discard();}else if(shot.level() instanceof ServerLevel world&&shot.tickCount%2==0){var v=shot.getDeltaMovement();for(int i=0;i<4;i++){var at=shot.position().subtract(v.scale(i*.2));world.sendParticles(i%2==0?ParticleTypes.FLAME:ParticleTypes.SMALL_FLAME,at.x,at.y,at.z,1,.015,.015,.015,0);}}}
        if(!(e.getEntity() instanceof ServerPlayer p))return;var s=RpgAbilities.profile(p);var c=CASTS.get(p.getUUID());
        if(c!=null&&(!p.isAlive()||c.player!=p||p.isSpectator()||c.world!=p.level()||s.rpgClass()!=c.profile.rpgClass())){clear(p);return;}
        if(s.rpgClass()==RpgClass.HIDROMANTE){
            if(p.tickCount%20==0){effect(p,MobEffects.WATER_BREATHING,40,0);if(p.isInWater()){effect(p,MobEffects.CONDUIT_POWER,40,0);if(s.mainUnlocked(0))effect(p,MobEffects.DOLPHINS_GRACE,40,0);}}
        }
        if(p.tickCount%20==0)Professions.modifier(p,net.minecraft.world.entity.ai.attributes.Attributes.WATER_MOVEMENT_EFFICIENCY,"hydro_swim",s.rpgClass()==RpgClass.HIDROMANTE&&s.hasSkill(2)?(s.mainUnlocked(0)?.5:.2)+(s.hasSkill(6)?.2:0):0,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
        if(c==null)return;long now=c.world.getGameTime();
        if(c.fireLanding>now){p.fallDistance=0;if(!p.onGround())c.launchAir=true;else if(c.launchAir){c.fireLanding=0;c.launchAir=false;}}
        if(c.profile.rpgClass()==RpgClass.ILUSIONISTA)illusionTick(c,now);
        else if(c.profile.rpgClass()==RpgClass.HIDROMANTE){if(c.until[1]>now&&p.tickCount%5==0)jet(c);if(c.until[1]<=now)discard(c.sea);}
        else fireTick(c,now);
        if(c.until[2]<=now)p.getPersistentData().remove("oxy_waterwalk");
        if(p.tickCount%10==0){sync(p);RpgAbilities.syncStatus(p);}
    }
    private static void illusionTick(Cast c,long now){
        var p=c.player;
        if(c.until[0]>now){
            if(c.disguise!=null){c.disguise.teleportTo(p.getX(),p.getY(),p.getZ());c.disguise.setYRot(p.getYRot());c.disguise.setYHeadRot(p.getYHeadRot());}
            c.copies.removeIf(m->{if(m.isAlive())return false;OWNERS.remove(m.getUUID());return true;});int index=0;for(var copy:c.copies){double angle=now*.025+index++*2;var point=p.position().add(Math.cos(angle)*2,0,Math.sin(angle)*2);var delta=point.subtract(copy.position());copy.move(MoverType.SELF,delta.normalize().scale(Math.min(.2,delta.length())));if(p.tickCount%20==0)for(var mob:near(c,copy.position(),2)){hit(c,mob,c.profile.hasSkill(6)?2:1);break;}}
            for(var mob:near(c,p.position(),32))if(mob.getTarget()==p)mob.setTarget(null);
        }else{discard(c.copies);if(c.disguise!=null){OWNERS.remove(c.disguise.getUUID());c.disguise.discard();c.disguise=null;if(!intangible(p))ghost(p,0);}}
        if(c.until[1]>now&&c.levitated!=null&&c.levitated.isAlive()&&p.tickCount%20==0){hit(c,c.levitated,c.profile.hasSkill(9)?3:2);if(c.profile.mainUnlocked(1))for(var mob:near(c,c.levitated.position(),c.profile.hasSkill(11)?5:3)){effect(mob,MobEffects.SLOWNESS,30,1);effect(mob,MobEffects.WEAKNESS,30,0);}}
        if(c.until[2]>now){
            p.getPersistentData().putLong("oxy_eclipse",c.until[2]);
            p.getPersistentData().remove("oxy_eclipse");if(c.world.noCollision(p,p.getBoundingBox()))c.safe=p.position();p.getPersistentData().putLong("oxy_eclipse",c.until[2]);
            int i=0;for(var bat:c.bats){double angle=now*.25+i++;bat.teleportTo(p.getX()+Math.cos(angle)*.8,p.getY()+.8+Math.sin(angle*2)*.3,p.getZ()+Math.sin(angle)*.8);}
            ghostFlight(p,p.getLastClientInput().jump(),p.isShiftKeyDown());
            Professions.modifier(p,net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,"eclipse_speed",c.profile.hasSkill(16)?.3:.15,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            if(p.tickCount%4==0){for(int ribbon=0;ribbon<2;ribbon++){double angle=now*.16+ribbon*Math.PI;double y=.35+.6*(1+Math.sin(angle*.7));c.world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,p.getX()+Math.cos(angle)*.7,p.getY()+y,p.getZ()+Math.sin(angle)*.7,1,.02,.02,.02,0);}c.world.sendParticles(ParticleTypes.SMOKE,p.getX(),p.getY()+1,p.getZ(),2,.4,.5,.4,.005);}
            for(var mob:near(c,p.position(),20))if(mob.getTarget()==p)mob.setTarget(null);
        }else if(!c.bats.isEmpty()){finishEclipse(c);}
    }
    private static void finishEclipse(Cast c){var p=c.player;p.setNoGravity(false);p.fallDistance=0;Professions.modifier(p,net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,"eclipse_speed",0,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);p.getPersistentData().remove("oxy_eclipse");p.getPersistentData().remove("oxy_phase_tier");if(!c.world.noCollision(p,p.getBoundingBox()))p.teleportTo(c.safe.x,c.safe.y,c.safe.z);discard(c.bats);ghost(p,c.until[0]>c.world.getGameTime()?(int)(c.until[0]-c.world.getGameTime()):0);}
    private static void jet(Cast c){
        var start=c.player.getEyePosition();var direction=c.jet==null?c.player.getLookAngle():c.jet;double range=(c.profile.mainUnlocked(1)?22:16)+(c.profile.hasSkill(11)?6:0);var end=c.world.clip(new ClipContext(start,start.add(direction.scale(range)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,c.player)).getLocation();
        for(double d=.65;d<start.distanceTo(end);d+=1.15){var at=start.add(direction.scale(d));c.world.sendParticles(RpgParticles.WATER_PULSE.get(),at.x,at.y,at.z,0,direction.x,direction.y,direction.z,1);c.world.sendParticles(ParticleTypes.BUBBLE,at.x,at.y,at.z,1,.07,.07,.07,0);}
        boolean counted=false;for(var mob:near(c,start.lerp(end,.5),range/2+2))if(mob.getBoundingBox().inflate(.65).clip(start,end).isPresent()){hit(c,mob,c.profile.mainUnlocked(1)?2.5f:1.5f);mob.push(direction.x*(c.profile.hasSkill(12)?.55:.3),.06,direction.z*(c.profile.hasSkill(12)?.55:.3));counted=true;}if(counted)ClassJourney.advance(c.player,1,1);
    }

    private static void fireTick(Cast c,long now){
        var p=c.player;
        if(p.tickCount%20==0){if(!p.isInWaterOrRain()&&c.profile.hasSkill(0))heat(c,Math.min(3,near(c,p.position(),12).stream().filter(Mob::isOnFire).count()));if(p.isInWaterOrRain())c.heat=Math.max(0,c.heat-8);else if(now>c.heated+80)c.heat=Math.max(0,c.heat-2);}
        if(c.until[0]<=now)c.fireworks.clear();
        if(!c.fireworks.isEmpty()){
            if(p.tickCount%4==0)for(var id:c.fireworks.keySet()){var mob=c.world.getEntity(id);if(mob!=null){double angle=now*.2;c.world.sendParticles(ParticleTypes.FIREWORK,mob.getX()+Math.cos(angle)*.6,mob.getY()+1,mob.getZ()+Math.sin(angle)*.6,2,0,0,0,.01);}}
            if(c.nextExplosion>0&&now>=c.nextExplosion){var entry=c.fireworks.entrySet().iterator().next();c.fireworks.remove(entry.getKey());var entity=c.world.getEntity(entry.getKey());if(entity instanceof Mob mob&&mob.isAlive()){explosion(c,mob.position(),c.profile.mainUnlocked(0)?3:2,4*entry.getValue(),true);if(c.profile.hasSkill(6)&&c.chain++<8)for(var next:near(c,mob.position(),5))if(next!=mob&&!c.fireworks.containsKey(next.getUUID())){c.fireworks.put(next.getUUID(),1);break;}}c.nextExplosion=c.fireworks.isEmpty()?0:now+6;}
        }
        c.cores.removeIf(core->core.expires<=now);for(var core:c.cores){if(core.target!=null){var entity=c.world.getEntity(core.target);if(entity!=null&&entity.isAlive())core.position=entity.position().add(0,.7,0);else core.target=null;}if(p.tickCount%4==0)c.world.sendParticles(ParticleTypes.FLAME,core.position.x,core.position.y+.1,core.position.z,3,.12,.12,.12,.01);}
        c.lines.removeIf(line->line.expires()<=now);if(p.tickCount%10==0)for(var line:c.lines){var delta=line.b().subtract(line.a());double distance=delta.length();for(double d=0;d<=distance;d+=.5){var at=line.a().add(delta.normalize().scale(d));c.world.sendParticles(ParticleTypes.FLAME,at.x,at.y+.1,at.z,1,0,0,0,0);}for(var mob:near(c,line.a().lerp(line.b(),.5),distance/2+1))if(mob.getBoundingBox().inflate(.4).clip(line.a(),line.b()).isPresent()){hit(c,mob,c.profile.hasSkill(13)?2:1);mob.igniteForSeconds(2);}}
        if(c.until[2]>now){
            effect(p,MobEffects.FIRE_RESISTANCE,4,0);if(p.isInLava()){var intent=p.getLastClientMoveIntent();double swim=c.profile.mainUnlocked(2)?.2:.12;p.setDeltaMovement(p.getDeltaMovement().scale(.65).add(intent.scale(swim)).add(0,p.isShiftKeyDown()?-.08:p.getLastClientInput().jump()?.16:.02,0));p.hurtMarked=true;}
            Professions.modifier(p,net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH,"phoenix_jump",.25,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
            if(!p.onGround()&&c.ground&&p.getKnownMovement().y>.1&&now>=c.nextBoost){c.nextBoost=now+20;c.fireLanding=now+200;c.launchAir=false;p.fallDistance=0;p.setDeltaMovement(p.getDeltaMovement().add(0,.7,0));p.hurtMarked=true;explosion(c,p.position(),2,c.profile.hasSkill(16)?4:2,false);}
            if(p.tickCount%5==0){var at=p.position();c.embers.add(at);if(c.embers.size()>24)c.embers.remove(0);for(int side:new int[]{-1,1})for(int i=1;i<5;i++)c.world.sendParticles(ParticleTypes.FLAME,p.getX()+side*i*.25,p.getY()+1.1+i*.1,p.getZ(),1,0,0,0,0);}
            if(p.tickCount%10==0){for(var at:c.embers){c.world.sendParticles(ParticleTypes.SMALL_FLAME,at.x,at.y+.1,at.z,1,0,0,0,0);for(var mob:near(c,at,.8)){hit(c,mob,1);mob.igniteForSeconds(2);}}if(p.isInWater()&&c.profile.hasSkill(19)){c.world.sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY()+1,p.getZ(),12,1,1,1,.03);for(var mob:near(c,p.position(),4))effect(mob,MobEffects.WEAKNESS,30,0);FluidPockets.drain(p,p.blockPosition(),1,40);}}
            c.ground=p.onGround();c.ending=true;
        }else if(c.ending){Professions.modifier(p,net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH,"phoenix_jump",0,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);c.ending=false;float damage=4+(c.profile.hasSkill(20)?Math.min(6,c.cores.size()):0);explosion(c,p.position(),3,damage,false);if(c.profile.hasSkill(20))detonate(c,true);c.embers.clear();}
    }
    public static void fall(LivingFallEvent e){if(e.getEntity() instanceof ServerPlayer p){var c=CASTS.get(p.getUUID());if(c!=null&&c.profile.rpgClass()==RpgClass.PIROMANTE&&c.fireLanding>p.level().getGameTime())e.setCanceled(true);}}
    public static void incoming(LivingIncomingDamageEvent e){
        if(temporary(e.getEntity())){if(e.getEntity().isInvulnerable()||e.getSource().getEntity() instanceof Player)e.setCanceled(true);return;}
        if(e.getEntity() instanceof ServerPlayer p&&intangible(p)&&!e.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)){e.setCanceled(true);return;}
        if(e.getSource().getEntity() instanceof ServerPlayer p&&intangible(p))e.setCanceled(true);
    }
    public static void damage(LivingDamageEvent.Pre e){if(Bleeding.damage(e.getSource()))return;
        if(e.getEntity() instanceof ServerPlayer p){var s=RpgAbilities.profile(p);float factor=1;if(s.rpgClass()==RpgClass.AVIARIO&&!p.onGround()&&e.getSource().is(DamageTypeTags.IS_PROJECTILE))factor=1.2f;if(s.rpgClass()==RpgClass.HIDROMANTE&&!p.isInWater()&&e.getSource().is(DamageTypeTags.IS_FIRE))factor=1.15f;if(s.rpgClass()==RpgClass.PIROMANTE&&e.getSource().is(net.minecraft.world.damagesource.DamageTypes.FREEZE))factor=1.2f;e.setNewDamage(e.getNewDamage()*factor);}
        if(e.getSource().getEntity() instanceof ServerPlayer p){var s=RpgAbilities.profile(p);float factor=1;if(s.rpgClass()==RpgClass.ILUSIONISTA&&e.getSource().getDirectEntity()==p)factor=.8f;if(s.rpgClass()==RpgClass.HIDROMANTE)factor=p.level().isRainingAt(p.blockPosition())?(p.level().isThundering()&&s.hasSkill(6)?1.4f:1.2f):p.isInWater()?1:.9f;if(s.rpgClass()==RpgClass.PIROMANTE){var c=CASTS.get(p.getUUID());factor=p.isInWater()? .8f:1+(c==null?0:c.heat*.002f);if(e.getSource().getDirectEntity()==p&&s.hasSkill(9))e.getEntity().igniteForSeconds(3);}if(s.rpgClass()==RpgClass.XAMA&&!p.getMainHandItem().isEmpty()&&e.getSource().getDirectEntity()==p)factor=.9f;e.setNewDamage(e.getNewDamage()*factor);}
    }
    public static void damagePost(LivingDamageEvent.Post e){if(Bleeding.damage(e.getSource()))return;if(e.getNewDamage()>0&&e.getSource().getEntity() instanceof ServerPlayer p&&RpgAbilities.profile(p).rpgClass()==RpgClass.PIROMANTE&&RpgAbilities.attackable(e.getEntity()))heat(cast(p,RpgAbilities.profile(p)),2);}
    public static void targeting(LivingChangeTargetEvent e){if(e.getNewAboutToBeSetTarget() instanceof ServerPlayer p&&(intangible(p)||disguised(p)))e.setNewAboutToBeSetTarget(null);}
    public static void attack(AttackEntityEvent e){if(intangible(e.getEntity()))e.setCanceled(true);}
    public static void rightItem(PlayerInteractEvent.RightClickItem e){if(intangible(e.getEntity()))e.setCanceled(true);}
    public static void rightBlock(PlayerInteractEvent.RightClickBlock e){if(intangible(e.getEntity()))e.setCanceled(true);}
    public static void interactSpecific(PlayerInteractEvent.EntityInteractSpecific e){if(intangible(e.getEntity()))e.setCanceled(true);}
    public static void interact(PlayerInteractEvent.EntityInteract e){if(intangible(e.getEntity()))e.setCanceled(true);}
    public static void breakBlock(BlockEvent.BreakEvent e){if(intangible(e.getPlayer()))e.setCanceled(true);}
    public static void join(EntityJoinLevelEvent e){if(!e.getLevel().isClientSide()&&e.getEntity() instanceof ServerPlayer p&&!CASTS.containsKey(p.getUUID())&&p.getPersistentData().contains("oxy_eclipse")){p.setNoGravity(false);p.getPersistentData().remove("oxy_eclipse");p.getPersistentData().remove("oxy_phase_tier");}if(!e.getLevel().isClientSide()&&temporary(e.getEntity())&&!OWNERS.containsKey(e.getEntity().getUUID()))e.setCanceled(true);}
    public static void drops(LivingDropsEvent e){if(temporary(e.getEntity()))e.setCanceled(true);}
    public static void experience(LivingExperienceDropEvent e){if(temporary(e.getEntity()))e.setCanceled(true);}
    private static void discard(List<Mob> mobs){for(var m:mobs){OWNERS.remove(m.getUUID());m.discard();}mobs.clear();}
    public static void clear(ServerPlayer p){Professions.modifier(p,net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH,"phoenix_jump",0,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);var c=CASTS.remove(p.getUUID());p.getPersistentData().remove("oxy_waterwalk");if(c!=null){finishEclipse(c);discard(c.copies);discard(c.sea);if(c.disguise!=null){OWNERS.remove(c.disguise.getUUID());c.disguise.discard();}ghost(p,0);}p.getPersistentData().remove("oxy_eclipse");p.getPersistentData().remove("oxy_phase_tier");SHOTS.entrySet().removeIf(e->e.getValue().player()==p);}
    public static void tracking(PlayerEvent.StartTracking e){if(e.getEntity() instanceof ServerPlayer viewer){if(e.getTarget() instanceof ServerPlayer p){var c=CASTS.get(p.getUUID());if(c!=null){int ticks=(int)Math.max(0,Math.max(c.until[0],c.until[2])-p.level().getGameTime());if(ticks>0&&c.profile.rpgClass()==RpgClass.ILUSIONISTA)PacketDistributor.sendToPlayer(viewer,new RpgNetwork.Ghost(p.getId(),ticks));}}else if(temporary(e.getTarget()))PacketDistributor.sendToPlayer(viewer,new RpgNetwork.MobLevel(e.getTarget().getId(),e.getTarget().getPersistentData().getBooleanOr("oxy_cosmetic",false)?-2:-1));}}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)clear(p);}
    public static void stop(ServerStoppedEvent e){for(var c:new ArrayList<>(CASTS.values()))clear(c.player);CASTS.clear();SHOTS.clear();OWNERS.clear();}
}
