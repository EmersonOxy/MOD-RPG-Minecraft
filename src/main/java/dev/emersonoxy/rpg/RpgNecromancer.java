package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Session-owned vanilla creatures. No permanent blocks, pets, loot or spawn-level modifications. */
public final class RpgNecromancer {
    private static final String OWNER="oxy_summon_owner", SLOT="oxy_summon_slot", WEB="oxy_necro_web";
    private static final Map<UUID,Group[]> GROUPS=new HashMap<>();
    private static final Map<UUID,Minion> MINIONS=new HashMap<>();
    private static final Map<UUID,WebCast> WEBS=new HashMap<>();
    private static final Map<UUID,Long> BLIND=new HashMap<>();
    private static final Map<UUID,Mob> BLIND_MOBS=new HashMap<>();
    private record WebCast(ServerPlayer owner,RpgProfile profile,long expires) {}
    private record LastStrike(UUID owner,long expires) {}
    private static final Map<UUID,LastStrike> STRIKES=new HashMap<>();
    public static boolean summonKill(DamageSource source,LivingEntity mob,ServerPlayer player){
        if(isSummon(source.getEntity())||isSummon(source.getDirectEntity()))return true;
        var last=STRIKES.get(mob.getUUID());return last!=null&&source.getEntity()==null&&last.owner().equals(player.getUUID())&&last.expires()>mob.level().getGameTime();
    }
    private static final class Group {
        final ServerPlayer player;final ServerLevel world;final RpgProfile profile;final int slot;
        final List<Minion> units=new ArrayList<>();
        final long expires;UUID target;long idle;
        Group(ServerPlayer p,RpgProfile s,int slot){player=p;world=(ServerLevel)p.level();profile=s;this.slot=slot;expires=world.getGameTime()+s.abilityDuration(slot);idle=world.getGameTime()+80;}
    }
    private static final class Minion {
        final Mob mob;final Group group;final int strikes;int hits;long next;
        Minion(Mob mob,Group g,int strikes,int index){this.mob=mob;group=g;this.strikes=strikes;next=g.world.getGameTime()+20+index*12;}
    }
    private RpgNecromancer(){}
    public static boolean necromancy(Entity entity){var unit=MINIONS.get(entity.getUUID());return unit!=null&&unit.group.profile.rpgClass()==RpgClass.NECROMANTE;}
    public static boolean isSummon(Entity entity){return entity!=null&&entity.getPersistentData().contains(OWNER);}
    public static ServerPlayer owner(DamageSource source){
        if(source.getEntity() instanceof ServerPlayer player)return player;
        Entity entity=isSummon(source.getEntity())?source.getEntity():isSummon(source.getDirectEntity())?source.getDirectEntity():null;
        if(entity==null||!(entity.level() instanceof ServerLevel world))return null;
        try{return world.getServer().getPlayerList().getPlayer(UUID.fromString(entity.getPersistentData().getStringOr(OWNER,"")));}catch(IllegalArgumentException e){return null;}
    }
    private static boolean valid(LivingEntity target){return target instanceof Mob&&target.isAlive()&&!isSummon(target);}
    private static LivingEntity target(Group g){
        if(g.target==null)return null;var entity=g.world.getEntity(g.target);
        return entity instanceof LivingEntity living&&valid(living)?living:null;
    }
    private static Group group(ServerPlayer p,int slot){var all=GROUPS.get(p.getUUID());return all==null?null:all[slot];}
    private static Group begin(ServerPlayer p,RpgProfile s,int slot){
        var all=GROUPS.computeIfAbsent(p.getUUID(),id->new Group[3]);remove(all[slot]);
        var g=new Group(p,s,slot);all[slot]=g;return g;
    }
    public static void animals(ServerPlayer p,RpgProfile profile,int count){
        var g=begin(p,profile,1);var types=List.of(EntityType.WOLF,EntityType.POLAR_BEAR,EntityType.FOX);
        for(int i=0;i<count;i++)spawn(g,types.get(p.getRandom().nextInt(types.size())),i,0);
    }
    public static void cleanse(LivingEntity entity){BLIND.remove(entity.getUUID());BLIND_MOBS.remove(entity.getUUID());}
    public static void activate(ServerPlayer p,RpgProfile s,int slot){
        if(slot==0){
            var all=GROUPS.get(p.getUUID());if(all!=null){remove(all[0]);all[0]=null;}
            var shot=new Snowball(p.level(),p,new ItemStack(Items.COBWEB));shot.setNoGravity(true);
            shot.getPersistentData().putBoolean(WEB,true);WEBS.put(shot.getUUID(),new WebCast(p,s,p.level().getGameTime()+120));
            AbilityAim.shoot(shot,p,1.7f);p.level().addFreshEntity(shot);
        }else if(slot==1){
            var g=begin(p,s,slot);
            int skeletons=s.mainUnlocked(1)?1+(s.hasSkill(11)?1:0)+(s.hasSkill(13)?1:0):0;
            int zombies=s.mainUnlocked(1)?3+(s.hasSkill(12)?1:0)+(s.hasSkill(13)?1:0):1+(s.hasSkill(9)?1:0);
            for(int i=0;i<zombies;i++)spawn(g,EntityType.ZOMBIE,i,0);
            for(int i=0;i<skeletons;i++)spawn(g,EntityType.SKELETON,i+zombies,0);
        }else{
            var g=begin(p,s,slot);
            double radius=(s.mainUnlocked(2)?6:3)+(s.hasSkill(18)?1:0);
            int duration=(s.mainUnlocked(2)?80+(s.hasSkill(20)?40:0):40+(s.hasSkill(16)?20:0));
            for(var mob:g.world.getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(radius),m->valid(m)&&p.distanceToSqr(m)<=radius*radius&&p.hasLineOfSight(m))){
                mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,duration,0));
                BLIND.put(mob.getUUID(),g.world.getGameTime()+duration);BLIND_MOBS.put(mob.getUUID(),mob);
                mob.setTarget(null);mob.getNavigation().stop();
                g.world.sendParticles(ParticleTypes.SMOKE,mob.getX(),mob.getEyeY(),mob.getZ(),12,.3,.3,.3,.03);
            }
            var aimed=aim(p,20);if(aimed!=null)g.target=aimed.getUUID();else{var near=nearest(g,p.position(),radius);if(near!=null)g.target=near.getUUID();}
            int count=s.mainUnlocked(2)?3:1;
            for(int i=0;i<count;i++)spawn(g,EntityType.PHANTOM,i,1);
        }
        ((ServerLevel)p.level()).sendParticles(ParticleTypes.SOUL,p.getX(),p.getY()+1,p.getZ(),20,.6,.8,.6,.03);
    }
    private static LivingEntity aim(ServerPlayer p,double range){
        Vec3 start=p.getEyePosition(),dir=AbilityAim.direction(p);LivingEntity best=null;double distance=range;
        for(var mob:p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(range),RpgNecromancer::valid)){
            double along=mob.getBoundingBox().getCenter().subtract(start).dot(dir);
            if(along<0||along>distance||!p.hasLineOfSight(mob))continue;
            if(mob.getBoundingBox().inflate(.4).clip(start,start.add(dir.scale(range))).isPresent()){best=mob;distance=along;}
        }return best;
    }
    private static LivingEntity nearest(Group g,Vec3 from,double range){
        LivingEntity best=null;double distance=range*range;
        for(var mob:g.world.getEntitiesOfClass(Mob.class,g.player.getBoundingBox().inflate(range),m->m instanceof Enemy&&valid(m))){
            double d=mob.distanceToSqr(from);if(d<distance&&g.player.hasLineOfSight(mob)){distance=d;best=mob;}
        }return best;
    }
    private static <T extends Mob> void spawn(Group g,EntityType<T> type,int index,int strikes){
        Mob mob=type.create(g.world,EntitySpawnReason.MOB_SUMMONED);if(mob==null)return;
        mob.getPersistentData().putString(OWNER,g.player.getUUID().toString());mob.getPersistentData().putInt(SLOT,g.slot);
        mob.targetSelector.removeAllGoals(goal->true);mob.setPersistenceRequired();mob.setInvulnerable(false);
        if(g.profile.rpgClass()==RpgClass.XAMA){
            mob.goalSelector.removeAllGoals(goal->true);
            mob.goalSelector.addGoal(0,new net.minecraft.world.entity.ai.goal.FloatGoal(mob));
            if(mob instanceof net.minecraft.world.entity.PathfinderMob animal)
                mob.goalSelector.addGoal(1,new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(animal,1.2,true));
        }
        mob.setCustomName(Component.literal(mob instanceof Phantom?"Phantom invocado":mob instanceof Spider?"Aranha invocada":mob instanceof AbstractSkeleton?"Esqueleto invocado":mob instanceof Zombie?"Zumbi invocado":mob.getType().getDescription().getString()+" invocado"));
        if(mob instanceof AbstractSkeleton)mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));
        if(mob instanceof Zombie zombie){zombie.setBaby(false);mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STONE_SWORD));}
        if(mob instanceof Zombie||mob instanceof AbstractSkeleton)mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.LEATHER_HELMET));
        for(var slot:new EquipmentSlot[]{EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND,EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})mob.setDropChance(slot,0);
        var health=mob.getAttribute(Attributes.MAX_HEALTH);if(health!=null){health.setBaseValue((mob instanceof Phantom?14:mob instanceof Spider?12:16)*(.65+.35*g.profile.level()/100.0));if(g.profile.hasSkill(7))health.setBaseValue(health.getBaseValue()*1.1);}
        var attack=mob.getAttribute(Attributes.ATTACK_DAMAGE);if(attack!=null)attack.setBaseValue(mob instanceof Phantom?4:3);
        if(g.profile.rpgClass()==RpgClass.NECROMANTE&&health!=null)health.setBaseValue(health.getBaseValue()*ClassEquipment.summonHealth(g.player));
        mob.setHealth(mob.getMaxHealth());mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,Integer.MAX_VALUE,0,false,false));
        if(mob instanceof Phantom){mob.setNoAi(true);mob.setNoGravity(true);}
        LivingEntity target=target(g);Vec3 center=g.slot==0&&target!=null?target.position():g.player.position();
        boolean found=false;double angle=index*2.399;
        for(int attempt=0;attempt<24;attempt++){
            double radius=1.4+attempt/8.0;
            double x=center.x+Math.cos(angle+attempt*.6)*radius,z=center.z+Math.sin(angle+attempt*.6)*radius;
            double y=center.y+(mob instanceof Phantom?2.5:attempt%3-1);
            mob.setPos(x,y,z);
            if(g.world.noCollision(mob,mob.getBoundingBox())&&(mob instanceof Phantom||!g.world.noCollision(mob,mob.getBoundingBox().move(0,-.7,0)))){found=true;break;}
        }
        if(!found)return;
        var unit=new Minion(mob,g,strikes,index);g.units.add(unit);MINIONS.put(mob.getUUID(),unit);
        if(!g.world.addFreshEntity(mob)){g.units.remove(unit);MINIONS.remove(mob.getUUID());return;}
        if(target!=null){mob.setTarget(target);if(mob instanceof Spider)mob.setDeltaMovement(target.position().subtract(mob.position()).multiply(1,0,1).normalize().scale(.5).add(0,.45,0));}
    }
    public static void impact(ProjectileImpactEvent event){
        var shot=event.getProjectile();if(!(shot.level() instanceof ServerLevel world)||!shot.getPersistentData().getBooleanOr(WEB,false)||event.isCanceled())return;
        event.setCanceled(true);var web=WEBS.remove(shot.getUUID());var s=web==null?null:web.profile();
        if(s!=null&&shot.getOwner() instanceof ServerPlayer p&&event.getRayTraceResult() instanceof EntityHitResult hit&&hit.getEntity() instanceof LivingEntity mob&&valid(mob)&&p.isAlive()){
            ClassJourney.advance(p,0,1);
            int duration=(s.mainUnlocked(0)?80:40)+(s.hasSkill(5)?40:0);
            mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,duration,3));
            TemporaryBlocks.web(p,mob,duration);
            var g=begin(p,s,0);g.target=mob.getUUID();mark(p,mob);
            int count=s.mainUnlocked(0)?3+(s.hasSkill(4)?1:0)+(s.hasSkill(6)?1:0):1+(s.hasSkill(2)?1:0);
            for(int i=0;i<count;i++)spawn(g,EntityType.SPIDER,i,0);
            world.sendParticles(ParticleTypes.CLOUD,mob.getX(),mob.getY()+.7,mob.getZ(),20,.5,.5,.5,.03);
            RpgAbilities.syncStatus(p);
        }
        shot.discard();
    }
    public static void join(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide())return;
        if(isSummon(event.getEntity())&&event.getEntity() instanceof Mob mob&&!MINIONS.containsKey(mob.getUUID())){event.setCanceled(true);return;}
        if(event.getEntity() instanceof Projectile shot&&isSummon(shot.getOwner())){
            shot.getPersistentData().putString(OWNER,shot.getOwner().getPersistentData().getStringOr(OWNER,""));
            shot.getPersistentData().putInt(SLOT,shot.getOwner().getPersistentData().getIntOr(SLOT,1));
        }
    }
    public static void tick(EntityTickEvent.Post event){
        if(!(event.getEntity().level() instanceof ServerLevel world))return;
        var entity=event.getEntity();long time=world.getGameTime();
        if(entity instanceof Projectile allyShot&&isSummon(allyShot)){
            var p=owner(new DamageSource(world.damageSources().magic().typeHolder(),allyShot,allyShot.getOwner()));
            int slot=allyShot.getPersistentData().getIntOr(SLOT,1);
            if(p==null||group(p,slot)==null||allyShot.tickCount>120){allyShot.discard();return;}
        }
        if(entity instanceof Projectile shot&&shot.getPersistentData().getBooleanOr(WEB,false)){
            if(shot.tickCount>120){WEBS.remove(shot.getUUID());shot.discard();}
            else if(shot.tickCount%2==0)world.sendParticles(ParticleTypes.CLOUD,shot.getX(),shot.getY(),shot.getZ(),2,.03,.03,.03,.01);
        }
        if(entity instanceof ServerPlayer p){
            WEBS.entrySet().removeIf(e->e.getValue().owner().getUUID().equals(p.getUUID())&&e.getValue().expires()<=time);
            if(p.tickCount%20==0)STRIKES.entrySet().removeIf(e->e.getValue().expires()<=time);
            if(p.tickCount%20==0)BLIND.entrySet().removeIf(e->{var mob=BLIND_MOBS.get(e.getKey());boolean gone=mob==null||!mob.isAlive()||e.getValue()<=mob.level().getGameTime();if(gone)BLIND_MOBS.remove(e.getKey());return gone;});
            var all=GROUPS.get(p.getUUID());if(all==null)return;
            for(int slot=0;slot<3;slot++){
                var g=all[slot];if(g==null)continue;
                g.units.removeIf(m->{boolean gone=!m.mob.isAlive()||m.mob.isRemoved();if(gone)MINIONS.remove(m.mob.getUUID());return gone;});
                if(!p.isAlive()||p.level()!=g.world||g.units.isEmpty()||slot!=0&&time>=g.expires){remove(g);all[slot]=null;continue;}
                var target=target(g);
                if(slot==0){
                    if(target!=null)g.idle=time+80;
                    else if(time>=g.idle){remove(g);all[slot]=null;continue;}
                }else if(target==null||p.distanceToSqr(target)>32*32){
                    var next=nearest(g,p.position(),16);g.target=next==null?null:next.getUUID();target=next;
                }
                if(p.tickCount%5==0)for(var m:g.units)if(!(m.mob instanceof Phantom)){
                    m.mob.setTarget(target);
                    if(target==null&&p.distanceToSqr(m.mob)>9)m.mob.getNavigation().moveTo(p,1.1);
                }
            }
            if(java.util.Arrays.stream(all).noneMatch(java.util.Objects::nonNull)){GROUPS.remove(p.getUUID());RpgAbilities.syncStatus(p);}
            else if(p.tickCount%10==0)RpgAbilities.syncStatus(p);
        }
        if(entity instanceof Mob mob){
            var blind=BLIND.get(mob.getUUID());if(blind!=null){if(time>=blind){BLIND.remove(mob.getUUID());BLIND_MOBS.remove(mob.getUUID());}else{mob.setTarget(null);mob.getNavigation().stop();}}
            if(!isSummon(mob))return;
            var unit=MINIONS.get(mob.getUUID());if(unit==null){mob.discard();return;}
            var g=unit.group;
            if(!g.player.isAlive()||g.player.level()!=world||mob.distanceToSqr(g.player)>64*64){discard(unit);return;}
            mob.clearFire();
            if(mob instanceof Phantom){
                var target=target(g);if(target==null){hover(unit,g.player.position().add(0,3,0));return;}
                if(time<unit.next){hover(unit,target.position().add(0,3,0));return;}
                Vec3 toward=target.getBoundingBox().getCenter().subtract(mob.position());
                mob.move(MoverType.SELF,toward.normalize().scale(Math.min(.65,toward.length())));
                mob.setDeltaMovement(Vec3.ZERO);
                mob.setYRot((float)(Math.toDegrees(Math.atan2(-toward.x,toward.z))));
                if(mob.getBoundingBox().inflate(.7).intersects(target.getBoundingBox())&&mob.hasLineOfSight(target)){
                    if(!target.hurtServer(world,g.player.damageSources().indirectMagic(mob,g.player),4)){unit.next=time+5;return;}
                    world.sendParticles(ParticleTypes.SOUL,target.getX(),target.getY()+1,target.getZ(),10,.3,.3,.3,.03);
                    unit.hits++;unit.next=time+40;
                    if(unit.hits>=unit.strikes)discard(unit);
                }
            }
        }
    }
    private static void hover(Minion m,Vec3 center){
        double angle=m.mob.tickCount*.07+m.mob.getId();Vec3 point=center.add(Math.cos(angle)*2,.2,Math.sin(angle)*2);
        Vec3 delta=point.subtract(m.mob.position());m.mob.move(MoverType.SELF,delta.normalize().scale(Math.min(.25,delta.length())));m.mob.setDeltaMovement(Vec3.ZERO);
    }
    private static void mark(ServerPlayer p,LivingEntity target){
        if(!valid(target))return;var all=GROUPS.get(p.getUUID());if(all==null)return;
        for(var g:all)if(g!=null){g.target=target.getUUID();g.idle=g.world.getGameTime()+80;for(var m:g.units)m.mob.setTarget(target);}
    }
    public static void attack(AttackEntityEvent event){if(event.getEntity() instanceof ServerPlayer p&&event.getTarget() instanceof LivingEntity target)mark(p,target);}
    public static void damage(LivingDamageEvent.Post event){if(Bleeding.damage(event.getSource()))return;if(event.getSource().getEntity() instanceof ServerPlayer p&&event.getNewDamage()>0&&!isSummon(event.getSource().getDirectEntity()))mark(p,event.getEntity());}
    public static void targeting(LivingChangeTargetEvent event){
        var mob=event.getEntity();var next=event.getNewAboutToBeSetTarget();
        if(isSummon(mob)){var unit=MINIONS.get(mob.getUUID());if(next!=null&&(unit==null||target(unit.group)!=next))event.setNewAboutToBeSetTarget(null);}
        else if(BLIND.getOrDefault(mob.getUUID(),0L)>mob.level().getGameTime())event.setNewAboutToBeSetTarget(null);
    }
    public static void incoming(LivingIncomingDamageEvent event){
        var source=event.getSource();var direct=source.getDirectEntity();var attacker=source.getEntity();
        if(isSummon(event.getEntity())){if(!(attacker instanceof Enemy)||isSummon(attacker)||isSummon(direct))event.setCanceled(true);return;}
        if(attacker instanceof ServerPlayer&&!isSummon(direct))STRIKES.remove(event.getEntity().getUUID());

        Entity ally=isSummon(attacker)?attacker:isSummon(direct)?direct:null;
        if(ally==null)return;
        var p=owner(source);int slot=ally.getPersistentData().getIntOr(SLOT,0);var g=p==null?null:group(p,slot);
        boolean allowed=g!=null&&valid(event.getEntity())&&(target(g)==event.getEntity()||slot==1&&event.getEntity() instanceof Enemy);
        if(!allowed){event.setCanceled(true);return;}
        event.getEntity().setLastHurtByPlayer(p,100);
        STRIKES.put(event.getEntity().getUUID(),new LastStrike(p.getUUID(),event.getEntity().level().getGameTime()+100));
        float factor=(g.profile.hasSkill(0)?1.05f:1)*(.65f+.35f*g.profile.level()/100f);
        if(slot==2&&g.profile.hasSkill(19))factor*=1.15f;
        if(g.profile.rpgClass()==RpgClass.NECROMANTE)factor*=ClassEquipment.summonDamage(p);
        event.setAmount(event.getAmount()*factor);
    }
    public static void drops(LivingDropsEvent event){if(isSummon(event.getEntity()))event.getDrops().clear();}
    public static void experience(LivingExperienceDropEvent event){if(isSummon(event.getEntity()))event.setDroppedExperience(0);}
    public static int remaining(ServerPlayer p,int slot){
        var g=group(p,slot);if(g==null&&slot==0)return WEBS.values().stream().filter(w->w.owner().getUUID().equals(p.getUUID())).mapToInt(w->(int)Math.max(0,w.expires()-p.level().getGameTime())).max().orElse(0);
        if(g==null||g.units.stream().noneMatch(m->m.mob.isAlive()&&!m.mob.isRemoved()))return 0;
        if(slot==0)return target(g)!=null?-1:(int)Math.max(0,g.idle-g.world.getGameTime());
        return (int)Math.max(0,g.expires-g.world.getGameTime());
    }
    private static void discard(Minion m){MINIONS.remove(m.mob.getUUID());if(!m.mob.isRemoved()){m.group.world.sendParticles(ParticleTypes.SOUL,m.mob.getX(),m.mob.getY()+.6,m.mob.getZ(),6,.2,.3,.2,.02);m.mob.discard();}}
    private static void remove(Group g){if(g!=null){for(var m:g.units)discard(m);g.units.clear();}}
    public static int phase(ServerPlayer p,int slot){return slot==0&&group(p,0)==null?1:6;}
    public static void death(LivingDeathEvent event){if(event.isCanceled())return;if(event.getEntity() instanceof ServerPlayer p)clear(p);else if(isSummon(event.getEntity()))MINIONS.remove(event.getEntity().getUUID());}
    public static void tracking(PlayerEvent.StartTracking event){if(event.getEntity() instanceof ServerPlayer p&&event.getTarget() instanceof Mob mob&&isSummon(mob))PacketDistributor.sendToPlayer(p,new RpgNetwork.MobLevel(mob.getId(),-1));}
    public static void clear(ServerPlayer p){WEBS.entrySet().removeIf(e->e.getValue().owner().getUUID().equals(p.getUUID()));var all=GROUPS.remove(p.getUUID());if(all!=null)for(var g:all)remove(g);}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer p)clear(p);}
    public static void stop(ServerStoppedEvent event){GROUPS.clear();MINIONS.clear();WEBS.clear();BLIND.clear();BLIND_MOBS.clear();STRIKES.clear();}
}





