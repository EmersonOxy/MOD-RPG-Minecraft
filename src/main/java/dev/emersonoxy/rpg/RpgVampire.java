package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.tags.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server-owned vampire forms, blood bonds and shielding; no permanent creative abilities. */
public final class RpgVampire {
    private static final Map<UUID,State> STATES=new HashMap<>();
    private static final Map<UUID,Long> ROOTS=new HashMap<>();
    private static final Map<ServerLevel,Map<BlockPos,Long>> TRAILS=new WeakHashMap<>();
    private static boolean ownEffect;
    private static final TagKey<DamageType> SPELLS=TagKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath("oxyrpg","spells"));
    private static final class State {
        final ServerPlayer p;final ServerLevel world;RpgProfile profile;long thirst,twilight,swarm,nextFeed,bondUntil,healUntil;float healing;UUID bond;Bat form;final List<Bat> bats=new ArrayList<>();boolean mayfly,flying;
        State(ServerPlayer p,RpgProfile s){this.p=p;world=p.level();profile=s;}
    }
    private RpgVampire(){}
    public static boolean proxy(Entity e){return e!=null&&e.getPersistentData().getBooleanOr("oxy_vampire_proxy",false);}
    public static boolean bat(Player p){return p.getPersistentData().getBooleanOr("oxy_vampire_bat",false);}
    public static boolean twilight(Player p){return p.getPersistentData().getLongOr("oxy_vampire_twilight",0)>p.level().getGameTime();}
    public static boolean extra(ServerPlayer p,int slot){return slot==1&&bat(p);}
    public static void validate(ServerPlayer p,int slot){
        if(bat(p)&&slot!=1)throw new IllegalStateException("Na forma de morcego, use Metamorfose para retornar.");
        if(RpgAbilities.profile(p).rpgClass()==RpgClass.VAMPIRESCO&&slot==0&&p.getHealth()>=p.getMaxHealth())throw new IllegalStateException("Sede de Sangue exige vida perdida.");
    }
    private static State state(ServerPlayer p,RpgProfile s){var old=STATES.get(p.getUUID());if(old==null||old.p!=p||old.world!=p.level()){clear(p);old=new State(p,s);STATES.put(p.getUUID(),old);}old.profile=s;return old;}
    public static void activate(ServerPlayer p,RpgProfile s,int slot){
        var c=state(p,s);long now=c.world.getGameTime();
        if(slot==0){c.thirst=now+s.abilityDuration(0);c.bond=null;}
        else if(slot==1){if(bat(p)){returnHuman(c,true);return;}c.mayfly=p.getAbilities().mayfly;c.flying=p.getAbilities().flying;p.getPersistentData().putBoolean("oxy_vampire_original_mayfly",c.mayfly);p.getPersistentData().putBoolean("oxy_vampire_original_flying",c.flying);p.closeContainer();c.form=spawn(c,p.position().add(0,.6,0));
            if(c.form!=null)c.form.setInvulnerable(true);p.getPersistentData().putBoolean("oxy_vampire_bat",true);p.getAbilities().mayfly=true;p.getAbilities().flying=true;p.onUpdateAbilities();ghost(p,72000);sync(c);}
        else {c.twilight=now+s.abilityDuration(2);p.getPersistentData().putLong("oxy_vampire_twilight",c.twilight);effect(p,MobEffects.STRENGTH,s.abilityDuration(2),s.mainUnlocked(2)?1:0);effect(p,MobEffects.SPEED,s.abilityDuration(2),s.hasSkill(18)?1:0);effect(p,MobEffects.HASTE,s.abilityDuration(2),s.hasSkill(19)?1:0);sync(c);}
        ClassJourney.advance(p,slot,1);
    }
    private static Bat spawn(State c,Vec3 at){var b=EntityType.BAT.create(c.world,EntitySpawnReason.TRIGGERED);if(b==null)return null;b.setPos(at);b.setResting(false);b.setNoAi(true);b.setNoGravity(true);b.noPhysics=true;b.setHealth(3+(c.profile.hasSkill(12)?2:0));b.getPersistentData().putBoolean("oxy_vampire_proxy",true);c.world.addFreshEntity(b);return b;}
    private static void ghost(ServerPlayer p,int ticks){var v=new RpgNetwork.Ghost(p.getId(),ticks);PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,v);}
    private static void sync(State c){PacketDistributor.sendToPlayer(c.p,new RpgNetwork.VampireState(bat(c.p),(int)Math.max(0,c.twilight-c.world.getGameTime()),c.bats.size()));}
    public static void syncStatus(ServerPlayer p){var c=STATES.get(p.getUUID());if(c==null){PacketDistributor.sendToPlayer(p,new RpgNetwork.VampireState(false,0,0));PacketDistributor.sendToPlayer(p,new RpgNetwork.AbilityStatus(0,0,0,0,0,0,0,0,0));return;}sync(c);long now=c.world.getGameTime();var s=c.profile;PacketDistributor.sendToPlayer(p,new RpgNetwork.AbilityStatus((int)Math.max(0,c.thirst-now),bat(p)?-1:(int)Math.max(0,c.swarm-now),(int)Math.max(0,c.twilight-now),s.abilityDuration(0),160,s.abilityDuration(2),c.thirst>now?5:0,bat(p)||c.swarm>now?6:0,c.twilight>now?5:0));}
    private static void returnHuman(State c,boolean swarm){var p=c.p;if(c.form!=null){c.form.discard();c.form=null;}p.getPersistentData().remove("oxy_vampire_bat");p.getAbilities().mayfly=p.isCreative()||p.isSpectator()||c.mayfly;p.getAbilities().flying=p.getAbilities().mayfly&&c.flying;p.onUpdateAbilities();p.getPersistentData().remove("oxy_vampire_original_mayfly");p.getPersistentData().remove("oxy_vampire_original_flying");p.fallDistance=0;p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,40,0));ghost(p,0);
        if(swarm){c.bats.forEach(Entity::discard);c.bats.clear();c.swarm=c.world.getGameTime()+160+(c.profile.hasSkill(13)?80:0);for(int i=0;i<(c.profile.mainUnlocked(1)?4:2)+(c.profile.hasSkill(11)?2:0);i++){var b=spawn(c,p.position().add(0,1,0));if(b!=null)c.bats.add(b);}}sync(c);}
    private static void effect(Player p,net.minecraft.core.Holder<MobEffect> effect,int ticks,int amp){ownEffect=true;try{p.addEffect(new MobEffectInstance(effect,ticks,amp,false,false));}finally{ownEffect=false;}}
    public static void tick(EntityTickEvent.Post event){
        var e=event.getEntity();if(!(e.level() instanceof ServerLevel world))return;long now=world.getGameTime();
        if(ROOTS.containsKey(e.getUUID())){if(ROOTS.get(e.getUUID())<=now||!e.isAlive())ROOTS.remove(e.getUUID());else if(e instanceof Mob m){m.getNavigation().stop();m.setDeltaMovement(0,m.getDeltaMovement().y,0);}}
        if(!(e instanceof ServerPlayer p))return;var s=RpgAbilities.profile(p);
        if(s.rpgClass()!=RpgClass.VAMPIRESCO||!p.isAlive()){if(STATES.containsKey(p.getUUID()))clear(p);return;}var c=state(p,s);
        if(bat(p)&&!s.canUseAbility(1))returnHuman(c,false);
        if(bat(p)){p.fallDistance=0;if(c.form!=null){c.form.setPos(p.position().add(0,.6,0));c.form.setYRot(p.getYRot());}if(p.tickCount%20==0&&!RpgDebug.unlimited(p)){if(!RpgResources.affordable(p,1))returnHuman(c,true);else RpgResources.spend(p,1);}}
        c.bats.removeIf(b->!b.isAlive()||b.isRemoved());if(c.swarm<=now&&!c.bats.isEmpty()){c.bats.forEach(Entity::discard);c.bats.clear();}
        for(int i=0;i<c.bats.size();i++){double a=now*.13+i*Math.PI*2/c.bats.size();c.bats.get(i).setPos(p.getX()+Math.cos(a)*1.1,p.getY()+1+Math.sin(a*1.5)*.25,p.getZ()+Math.sin(a)*1.1);}
        if(p.tickCount%20!=0)return;
        if(c.healUntil>now&&c.healing>0){float n=Math.min(c.healing,s.hasSkill(5)?1.5f:1);p.heal(n);c.healing-=n;}
        boolean day=world.dimensionType().hasSkyLight()&&Math.floorMod(world.getDayTime(),24000)<12000;
        if(day){effect(p,MobEffects.WEAKNESS,60,0);if(!p.isCreative()&&!p.isSpectator()&&!world.isRainingAt(p.blockPosition())&&world.canSeeSky(p.blockPosition().above()))p.hurtServer(world,p.damageSources().onFire(),s.hasSkill(6)?.5f:1);}
        else effect(p,MobEffects.NIGHT_VISION,240,0);
        var trails=TRAILS.get(world);boolean following=twilight(p)&&trails!=null&&nearTrail(trails,p.position(),now);
        Professions.modifier(p,Attributes.MOVEMENT_SPEED,"vampire_trail",following?.15+(s.hasSkill(20)?.1:0):0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        if(trails!=null&&p.tickCount%100==0)trails.entrySet().removeIf(t->t.getValue()<=now);
        if(p.getFoodData().getFoodLevel()<20&&!bat(p)){int moved=0;for(var animal:world.getEntitiesOfClass(Animal.class,p.getBoundingBox().inflate(8),a->a.isAlive()&&!(a instanceof TamableAnimal tame&&tame.isTame())&&Math.floorMod(a.getUUID().hashCode(),3)==0)){if(moved++>=6)break;var away=animal.position().subtract(p.position()).multiply(1,0,1).normalize();animal.getNavigation().moveTo(animal.getX()+away.x*5,animal.getY(),animal.getZ()+away.z*5,1.2);}}
        sync(c);
        PacketDistributor.sendToPlayer(p,new RpgNetwork.AbilityStatus((int)Math.max(0,c.thirst-now),bat(p)?-1:(int)Math.max(0,c.swarm-now),(int)Math.max(0,c.twilight-now),s.abilityDuration(0),160,s.abilityDuration(2),c.thirst>now?5:0,bat(p)||c.swarm>now?6:0,c.twilight>now?5:0));
    }
    private static boolean nearTrail(Map<BlockPos,Long> points,Vec3 at,long now){var center=BlockPos.containing(at);for(int x=-3;x<=3;x++)for(int y=-1;y<=1;y++)for(int z=-3;z<=3;z++){var pos=center.offset(x,y,z);if(pos.distToCenterSqr(at)<12&&points.getOrDefault(pos,0L)>now)return true;}return false;}
    public static void trail(ServerLevel world,Vec3 at){var points=TRAILS.computeIfAbsent(world,w->new HashMap<>());if(points.size()>=8192)points.entrySet().removeIf(t->t.getValue()<=world.getGameTime());if(points.size()<8192)points.put(BlockPos.containing(at),world.getGameTime()+1200);}
    public static void damage(LivingDamageEvent.Pre event){if(Bleeding.damage(event.getSource()))return;if(event.getSource().getDirectEntity() instanceof ServerPlayer p){var c=STATES.get(p.getUUID());if(c!=null&&c.thirst>p.level().getGameTime()&&event.getEntity().getUUID().equals(c.bond)&&c.bondUntil>p.level().getGameTime())event.setNewDamage(event.getNewDamage()*(c.profile.hasSkill(4)?1.4f:1.2f));}}
    public static void hit(LivingDamageEvent.Post event){
        if(event.getNewDamage()<=0||Bleeding.damage(event.getSource())||!(event.getSource().getDirectEntity() instanceof ServerPlayer p)||event.getSource().getEntity()!=p||!event.getSource().is(DamageTypes.PLAYER_ATTACK)||RpgAbilities.secondaryHit())return;
        var s=RpgAbilities.profile(p);var target=event.getEntity();if(s.rpgClass()!=RpgClass.VAMPIRESCO||!(target instanceof Mob)||RpgNecromancer.isSummon(target)||RpgSpecialists.temporary(target)||proxy(target))return;var c=state(p,s);long now=c.world.getGameTime();Bleeding.apply(target,p,0);
        if(!(target instanceof net.minecraft.world.entity.monster.Enemy)&&now>=c.nextFeed){p.getFoodData().eat(2+(s.hasSkill(2)?1:0),.3f);c.nextFeed=now+20;}
        if(c.thirst<=now)return;
        if(target.getUUID().equals(c.bond)&&c.bondUntil>now){ROOTS.put(target.getUUID(),now+(s.hasSkill(6)?60:40));target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,60,9));}
        else {float heal=Math.min(4,event.getNewDamage()*.15f+.5f+s.level()*.01f);p.heal(heal);c.healing=Math.min(p.getMaxHealth()*(s.hasSkill(5)?.25f:.15f),4+s.level()*.04f);c.healUntil=now+s.abilityDuration(0);c.bond=target.getUUID();c.bondUntil=c.thirst;Bleeding.apply(target,p,s.mainUnlocked(0)?1:0);}
    }
    public static void incoming(LivingIncomingDamageEvent event){
        var source=event.getSource();if(source.getEntity()!=null&&ROOTS.getOrDefault(source.getEntity().getUUID(),0L)>event.getEntity().level().getGameTime()){event.setCanceled(true);return;}
        if(!(event.getEntity() instanceof ServerPlayer p))return;var c=STATES.get(p.getUUID());if(c==null)return;
        if(twilight(p)){if(source.is(SPELLS)){event.setCanceled(true);return;}event.setAmount(event.getAmount()*1.25f);}
    }
    public static void shield(LivingIncomingDamageEvent event){
        if(event.isCanceled()||!(event.getEntity() instanceof ServerPlayer p))return;var source=event.getSource();var c=STATES.get(p.getUUID());if(c==null)return;
        if(!bat(p)&&!c.bats.isEmpty()&&source.getEntity()!=null&&!Bleeding.damage(source)&&!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)){
            var b=c.bats.stream().filter(Entity::isAlive).findFirst().orElse(null);if(b!=null){float absorbed=Math.min(b.getHealth(),event.getAmount());b.hurtServer(c.world,source,absorbed);event.setAmount(Math.max(0,event.getAmount()-absorbed));if(event.getAmount()<=0)event.setCanceled(true);sync(c);}}
    }
    public static void applicable(MobEffectEvent.Applicable event){if(!ownEffect&&event.getEntity() instanceof Player p&&twilight(p)&&!event.getEffectInstance().getEffect().is(Bleeding.EFFECT.getKey()))event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);}
    public static void useItem(LivingEntityUseItemEvent.Start event){if(event.getEntity() instanceof ServerPlayer p&&(bat(p)||RpgAbilities.profile(p).rpgClass()==RpgClass.VAMPIRESCO&&event.getItem().has(net.minecraft.core.component.DataComponents.FOOD)))event.setCanceled(true);}
    public static void attack(AttackEntityEvent e){if(bat(e.getEntity()))e.setCanceled(true);}
    public static void rightItem(PlayerInteractEvent.RightClickItem e){if(bat(e.getEntity()))e.setCanceled(true);}
    public static void rightBlock(PlayerInteractEvent.RightClickBlock e){if(bat(e.getEntity()))e.setCanceled(true);}
    public static void interact(PlayerInteractEvent.EntityInteract e){if(bat(e.getEntity()))e.setCanceled(true);}
    public static void specific(PlayerInteractEvent.EntityInteractSpecific e){if(bat(e.getEntity()))e.setCanceled(true);}
    public static void breaking(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent e){if(bat(e.getPlayer()))e.setCanceled(true);}
    public static void tracking(PlayerEvent.StartTracking e){if(e.getEntity() instanceof ServerPlayer viewer){if(e.getTarget() instanceof Player p&&bat(p))PacketDistributor.sendToPlayer(viewer,new RpgNetwork.Ghost(p.getId(),72000));else if(proxy(e.getTarget()))PacketDistributor.sendToPlayer(viewer,new RpgNetwork.MobLevel(e.getTarget().getId(),-2));}}
    public static void clear(ServerPlayer p){var c=STATES.remove(p.getUUID());if(c!=null){if(bat(p))returnHuman(c,false);c.bats.forEach(Entity::discard);}if(c==null&&bat(p)){p.getAbilities().mayfly=p.isCreative()||p.isSpectator()||p.getPersistentData().getBooleanOr("oxy_vampire_original_mayfly",false);p.getAbilities().flying=p.getAbilities().mayfly&&p.getPersistentData().getBooleanOr("oxy_vampire_original_flying",false);p.onUpdateAbilities();ghost(p,0);}p.getPersistentData().remove("oxy_vampire_original_mayfly");p.getPersistentData().remove("oxy_vampire_original_flying");p.getPersistentData().remove("oxy_vampire_bat");p.getPersistentData().remove("oxy_vampire_twilight");
        if(c!=null){p.removeEffect(MobEffects.STRENGTH);p.removeEffect(MobEffects.SPEED);p.removeEffect(MobEffects.HASTE);p.removeEffect(MobEffects.WEAKNESS);p.removeEffect(MobEffects.NIGHT_VISION);}
        Professions.modifier(p,Attributes.MOVEMENT_SPEED,"vampire_trail",0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);if(c!=null)PacketDistributor.sendToPlayer(p,new RpgNetwork.VampireState(false,0,0));}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)clear(p);}
    public static void stop(ServerStoppedEvent e){for(var c:new ArrayList<>(STATES.values()))clear(c.p);STATES.clear();ROOTS.clear();TRAILS.clear();}
}
