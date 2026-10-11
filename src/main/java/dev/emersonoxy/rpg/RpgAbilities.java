package dev.emersonoxy.rpg;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server-authoritative skills using vanilla entities, items, effects, particles and sounds. */
public final class RpgAbilities {
    private static final Map<UUID,RpgProfile> PROFILES = new HashMap<>();
    private static final Map<UUID,Active> ACTIVE = new HashMap<>();
    private static final Map<UUID,Frost> FROST = new HashMap<>();
    private static boolean secondaryDamage;
    static boolean secondaryHit(){return secondaryDamage;}
    private static final class Active {
        final long[] until = new long[3];
        final int[] total = new int[3], phase = new int[3], projectile = new int[3];
        final RpgProfile[] cast = new RpgProfile[3];
        long impetus, passiveHeal, passiveFire, passiveThorns;
        int crouchTicks, dashes, quakePulse, replicas;
        float hunger;
        Vec3 quakeOrigin;
        ServerLevel quakeLevel;
        final List<BlockPos> blocks = new ArrayList<>();
        MobEffectInstance previousInvisibility;
        long previousInvisibleUntil;
        boolean ghost, statusWasActive;
    }
    private record Frost(UUID owner, long until, long paralyze, double x, double z) {}
    private RpgAbilities() {}
    private static long now(ServerPlayer p) { return p.level().getGameTime(); }
    private static Active active(ServerPlayer p) { return ACTIVE.computeIfAbsent(p.getUUID(), id -> new Active()); }
    public static void updateProfile(ServerPlayer p, RpgProfile profile) { PROFILES.put(p.getUUID(), profile); }
    static RpgProfile profile(ServerPlayer p) { return PROFILES.getOrDefault(p.getUUID(), RpgProfile.NEW); }
    public static boolean attackable(LivingEntity e) { return e instanceof Mob && e.isAlive()&&!RpgNecromancer.isSummon(e)&&!RpgSpecialists.temporary(e); }
    private static boolean enemy(LivingEntity e){return attackable(e);}
    private static List<LivingEntity> near(ServerLevel level, Vec3 center, double radius) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(center,center).inflate(radius),
            e -> enemy(e) && e.distanceToSqr(center) <= radius * radius);
    }
    public static boolean extraDash(ServerPlayer p, int slot) {
        Active a = ACTIVE.get(p.getUUID());
        return slot == 2 && profile(p).rpgClass() == RpgClass.ARQUEIRO && a != null && a.until[2] > now(p) && a.dashes > 0;
    }
    public static void activate(ServerPlayer p, RpgProfile s, int slot, int direction) {
        if(s.rpgClass()==RpgClass.AVIARIO){RpgAviary.activate(p,s,slot);return;}
        if(RpgSpecialists.handles(s.rpgClass())){RpgSpecialists.activate(p,s,slot);return;}
        Active a = active(p);
        if (extraDash(p,slot)) { a.dashes--; dash(p,a.cast[2],direction); return; }
        a.cast[slot] = s;
        a.until[slot] = now(p) + s.abilityDuration(slot);
        a.total[slot]=s.abilityDuration(slot);a.phase[slot]=s.rpgClass()==RpgClass.MAGO ? (slot==1&&s.mainUnlocked(1)?2:slot==2?4:3) : 3;
        p.swing(InteractionHand.MAIN_HAND,true);
        ServerLevel level = (ServerLevel)p.level();
        switch (s.rpgClass()) {
            case MAGO -> {
                if (slot == 0) launch(p,s,1,new ItemStack(Items.ICE),1.7f);
                else if (slot == 1) {
                    if (s.mainUnlocked(1)) fire(p,s); else launch(p,s,3,new ItemStack(Items.FIRE_CHARGE),1.5f);
                    sound(p,SoundEvents.BLAZE_SHOOT,.8f);
                } else {
                    clearCracks(p,a);
                    a.quakeOrigin = p.position(); a.quakeLevel = level; a.quakePulse = 0; a.replicas = s.hasSkill(20) ? 2 : 0;
                    if (s.mainUnlocked(2)) selectBlocks(p,a);
                    quake(p,a,false); sound(p,SoundEvents.GENERIC_EXPLODE.value(),.6f);
                }
            }
            case GUERREIRO -> {
                if (slot == 0) {
                    int hits = 0; double radius = 3 + (s.hasSkill(4) ? .5 : 0)+Math.max(0,ClassEquipment.active(p,RpgClass.GUERREIRO)-1)*.5;
                    if (s.mainUnlocked(0)) for (LivingEntity e : near(level,p.position(),radius)) if(p.hasLineOfSight(e)) {
                        if(e.hurtServer(level,p.damageSources().playerAttack(p),s.abilityDamage()*(s.hasSkill(5)?1.1f:1))) hits++;
                        level.sendParticles(ParticleTypes.CRIT,e.getX(),e.getY()+1,e.getZ(),8,.3,.3,.3,.05);
                    }
                    if(hits>=2)ClassJourney.advance(p,1,1);
                    int duration = s.abilityDuration(0)+(s.hasSkill(6)?Math.min(5,hits)*20:0);
                    a.until[0]=now(p)+duration;a.total[0]=duration;
                    p.addEffect(new MobEffectInstance(MobEffects.STRENGTH,duration,0));
                    if(s.mainUnlocked(0))level.sendParticles(ParticleTypes.SWEEP_ATTACK,p.getX(),p.getY()+1,p.getZ(),6,radius/2,.1,radius/2,0);
                    if(s.mainUnlocked(0))sound(p,SoundEvents.PLAYER_ATTACK_SWEEP,1);
                } else if (slot == 1) {
                    a.hunger = 0;
                    if(s.mainUnlocked(1)) { p.getFoodData().setSaturation(0); p.addEffect(new MobEffectInstance(MobEffects.HUNGER,s.abilityDuration(1),0)); }
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,p.getX(),p.getY()+1,p.getZ(),15,.5,.7,.5,.1);
                    sound(p,SoundEvents.GENERIC_EAT.value(),.8f);
                } else {
                    p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,s.abilityDuration(2),s.mainUnlocked(2)?2:0));
                    p.addEffect(new MobEffectInstance(MobEffects.GLOWING,s.abilityDuration(2),0));
                    if(s.mainUnlocked(2)) p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,s.abilityDuration(2),0));
                    sound(p,SoundEvents.SHIELD_BLOCK.value(),1);
                }
            }
            case ARQUEIRO -> {
                if(slot == 0) launch(p,s,2,new ItemStack(Items.SPECTRAL_ARROW),3.3f);
                else if(slot == 1) {
                    startGhost(p,a,s.abilityDuration(1));
                    smoke(p,s); sound(p,SoundEvents.ILLUSIONER_MIRROR_MOVE,.8f);
                } else {
                    a.dashes = s.hasSkill(20)?1:0;
                    dash(p,s,direction);
                    p.addEffect(new MobEffectInstance(MobEffects.SPEED,s.abilityDuration(2),s.mainUnlocked(2)?1:0));
                }
            }
            case NECROMANTE -> {RpgNecromancer.activate(p,s,slot);a.phase[slot]=6;}
            case XAMA -> RpgShaman.activate(p,s,slot);
            default -> throw new IllegalStateException("Escolha uma classe primeiro.");
        }
    }
    private static void sound(ServerPlayer p, net.minecraft.sounds.SoundEvent sound,float volume) {
        p.level().playSound(null,p.blockPosition(),sound,SoundSource.PLAYERS,volume,1);
    }
    private static void launch(ServerPlayer p,RpgProfile s,int type,ItemStack item,float speed) {
        var level = (ServerLevel)p.level();
        Projectile shot = type == 2 ? new SpectralArrow(level,p,item,null) : new Snowball(level,p,item);
        if(type != 2) shot.setNoGravity(true);
        var tag = shot.getPersistentData();
        tag.putInt("oxy_type",type); tag.putLong("oxy_skills",s.skills()); tag.putInt("oxy_class",s.rpgClass().ordinal());
        tag.putInt("oxy_hits",0); tag.putFloat("oxy_bonus",1);
        AbilityAim.shoot(shot,p,speed);
        level.addFreshEntity(shot);
        var a=active(p);int slot=type==3?1:0;a.projectile[slot]=shot.getId();a.until[slot]=now(p)+120;a.total[slot]=120;a.phase[slot]=1;
        sound(p,type==2?SoundEvents.ARROW_SHOOT:SoundEvents.EVOKER_CAST_SPELL,1);
    }
    private static void dash(ServerPlayer p,RpgProfile s,int direction) {
        Vec3 dir = p.getLookAngle().multiply(1,0,1).normalize().yRot((float)(-Math.floorMod(direction,8)*Math.PI/4));
        if(dir.lengthSqr()<.01) dir = new Vec3(0,0,1).yRot((float)Math.toRadians(-p.getYRot()));
        double distance = s.mainUnlocked(2)?5+(s.hasSkill(18)?1:0):2;
        Vec3 start = p.position(); double safe = 0;
        for(double step=.25;step<=distance;step+=.25) {
            if(!p.level().noCollision(p,p.getBoundingBox().move(dir.scale(step)))) break;
            safe = step;
        }
        Vec3 end = start.add(dir.scale(safe));
        if(safe>0)ClassJourney.advance(p,2,1);
        p.teleportTo(end.x,end.y,end.z);
        var level = (ServerLevel)p.level();
        for(double step=0;step<safe;step+=.4) {var at=start.add(dir.scale(step));level.sendParticles(ParticleTypes.CLOUD,at.x,at.y+.5,at.z,2,.1,.2,.1,.02);}
        sound(p,SoundEvents.PLAYER_ATTACK_SWEEP,.7f);
    }
    private static void fire(ServerPlayer p,RpgProfile s) {
        var level=(ServerLevel)p.level(); Vec3 start=p.getEyePosition(),dir=AbilityAim.direction(p);
        double range=6+(s.hasSkill(11)?1:0)+(s.hasSkill(13)?1:0);
        Vec3 end=level.clip(new ClipContext(start,start.add(dir.scale(range)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();
        double length=start.distanceTo(end);
        for(double d=.3;d<length;d+=.35){var at=start.add(dir.scale(d));level.sendParticles(ParticleTypes.FLAME,at.x,at.y,at.z,3,.12,.12,.12,.02);}
        int hits=0;
        for(var e:level.getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(1.6),RpgAbilities::enemy)) {
            Vec3 relative=e.getBoundingBox().getCenter().subtract(start); double projection=relative.dot(dir);
            double width=.4+projection*(s.hasSkill(13)?.18:.1);
            if(projection>=0&&projection<=length&&relative.lengthSqr()-projection*projection<=width*width&&p.hasLineOfSight(e)) {
                if(e.hurtServer(level,p.damageSources().indirectMagic(p,p),s.hasSkill(12)?2.2f:2))hits++;
                e.igniteForSeconds(s.hasSkill(13)?10:6);
            }
        }
        if(hits>=2)ClassJourney.advance(p,1,1);
    }
    private static void selectBlocks(ServerPlayer p,Active a) {
        BlockPos origin=BlockPos.containing(a.quakeOrigin).below();
        int radius=a.cast[2].hasSkill(18)?5:4;
        for(int[] direction:new int[][]{{1,0},{0,1},{-1,0},{0,-1}}){
            boolean selected=false;
            for(int distance=2;distance<=radius&&!selected;distance++)for(int dy:new int[]{0,1,-1}){
                var pos=origin.offset(direction[0]*distance,dy,direction[1]*distance);
                if(!allowedBlock(a.quakeLevel,pos)||!a.quakeLevel.mayInteract(p,pos)||!a.quakeLevel.getBlockState(pos.above()).getCollisionShape(a.quakeLevel,pos.above()).isEmpty())continue;
                a.blocks.add(pos.immutable());selected=true;break;
            }
        }
    }
    private static boolean allowedBlock(ServerLevel level,BlockPos pos) {
        var state=level.getBlockState(pos);
        return !state.hasBlockEntity()&&(state.is(Blocks.STONE)||state.is(Blocks.DIRT)||state.is(Blocks.GRASS_BLOCK)||state.is(Blocks.SAND)||state.is(Blocks.GRAVEL));
    }
    private static int crackId(ServerPlayer p,BlockPos pos){return (p.getId()*31^pos.hashCode())&0x7fffffff;}
    private static void clearCracks(ServerPlayer p,Active a){if(a.quakeLevel!=null)for(var pos:a.blocks)a.quakeLevel.destroyBlockProgress(crackId(p,pos),pos,-1);a.blocks.clear();}
    private static void quake(ServerPlayer p,Active a,boolean replica) {
        var s=a.cast[2]; if(s==null||a.quakeLevel!=p.level())return;
        double radius=s.mainUnlocked(2)?4+(s.hasSkill(18)?1:0):1.5+(s.hasSkill(16)?.5:0);
        if(replica)radius*=.7;
        if(replica||a.quakePulse==0)SkillEffects.impact(a.quakeLevel,a.quakeOrigin,radius,8);
        int hits=0;
        for(var e:near(a.quakeLevel,a.quakeOrigin,radius)) if(p.hasLineOfSight(e)) {
            if(e.hurtServer(a.quakeLevel,p.damageSources().indirectMagic(p,p),s.mainUnlocked(2)?replica?1.5f:2:1))hits++;
            e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,50,s.mainUnlocked(2)?1:0));
        }
        if(hits>=2)ClassJourney.advance(p,2,1);
        a.quakeLevel.sendParticles(ParticleTypes.CRIT,a.quakeOrigin.x,a.quakeOrigin.y+.2,a.quakeOrigin.z,25,radius,.1,radius,.03);
        if(!replica&&s.mainUnlocked(2)) {
            a.quakePulse++;
            for(var pos:a.blocks) if(allowedBlock(a.quakeLevel,pos)&&a.quakeLevel.mayInteract(p,pos)) {
                int progress=a.quakePulse*2;
                if(progress==10) {a.quakeLevel.destroyBlockProgress(crackId(p,pos),pos,-1);a.quakeLevel.destroyBlock(pos,true,p);}
                else if(progress<10) a.quakeLevel.destroyBlockProgress(crackId(p,pos),pos,progress);
            }
        }
    }
    private static void startGhost(ServerPlayer p,Active a,int duration) {
        if(!a.ghost) {a.previousInvisibility=p.getEffect(MobEffects.INVISIBILITY);a.previousInvisibleUntil=a.previousInvisibility==null?0:now(p)+a.previousInvisibility.getDuration();}
        a.ghost=true;a.until[1]=now(p)+duration;a.total[1]=duration;a.phase[1]=3;
        p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,duration,0));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new RpgNetwork.Ghost(p.getId(),duration));
    }
    private static void stopGhost(ServerPlayer p,Active a) {
        if(!a.ghost)return;
        a.ghost=false;a.until[1]=0;p.removeEffect(MobEffects.INVISIBILITY);
        if(a.previousInvisibility!=null&&a.previousInvisibleUntil>now(p)) p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
            (int)(a.previousInvisibleUntil-now(p)),a.previousInvisibility.getAmplifier(),a.previousInvisibility.isAmbient(),a.previousInvisibility.isVisible()));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new RpgNetwork.Ghost(p.getId(),0));
    }
    public static void syncGhost(ServerPlayer p) {var a=ACTIVE.get(p.getUUID());if(a!=null&&a.ghost)PacketDistributor.sendToPlayer(p,new RpgNetwork.Ghost(p.getId(),(int)Math.max(0,a.until[1]-now(p))));}
    public static void tracking(PlayerEvent.StartTracking event) {if(event.getEntity() instanceof ServerPlayer viewer&&event.getTarget() instanceof ServerPlayer target){var a=ACTIVE.get(target.getUUID());if(a!=null&&a.ghost)PacketDistributor.sendToPlayer(viewer,new RpgNetwork.Ghost(target.getId(),(int)Math.max(0,a.until[1]-now(target))));}}
    private static void smoke(ServerPlayer p,RpgProfile s) {((ServerLevel)p.level()).sendParticles(ParticleTypes.SMOKE,p.getX(),p.getY()+.4,p.getZ(),30,s.hasSkill(12)?1.5:1,.5,s.hasSkill(12)?1.5:1,.04);}
    public static void killed(ServerPlayer p) {var s=profile(p);if(s.rpgClass()==RpgClass.GUERREIRO&&s.hasSkill(1))active(p).impetus=now(p)+40+(s.hasSkill(2)?20:0);}
    public static void onTick(EntityTickEvent.Post event){
        var entity=event.getEntity();if(!(entity.level() instanceof ServerLevel))return;
        if(!(entity instanceof ServerPlayer)&&!(entity instanceof Projectile)
            &&!FROST.containsKey(entity.getUUID()))return;
        RpgProfiler.run("abilities",()->onTickBody(event));
    }
    private static void onTickBody(EntityTickEvent.Post event){
        if(!(event.getEntity().level() instanceof ServerLevel level))return;
        if(event.getEntity() instanceof ServerPlayer p) {RpgResources.tick(p);tickPlayer(p);return;}
        if(event.getEntity() instanceof Projectile shot) {
            int type=shot.getPersistentData().getIntOr("oxy_type",0);if(type==0)return;
            if(shot.tickCount>=120){finishProjectile(shot,false);shot.discard();return;}
            if(shot.tickCount%2==0){var velocity=shot.getDeltaMovement();for(int i=0;i<4;i++){var at=shot.position().subtract(velocity.scale(i*.2));level.sendParticles(type==1?ParticleTypes.SNOWFLAKE:type==2?ParticleTypes.END_ROD:ParticleTypes.FLAME,at.x,at.y,at.z,1,.025,.025,.025,.005);}}
        }
        if(event.getEntity() instanceof LivingEntity e) {
            long time=level.getGameTime();var frost=FROST.get(e.getUUID());
            if(frost!=null) {
                if(!e.isAlive()||time>=frost.until){FROST.remove(e.getUUID());e.setTicksFrozen(0);}
                else {
                    e.setTicksFrozen(Math.max(0,e.getTicksRequiredToFreeze()-1));
                    if(time<frost.paralyze){if(e instanceof Mob mob)mob.getNavigation().stop();e.setDeltaMovement(0,e.getDeltaMovement().y,0);e.teleportTo(frost.x,e.getY(),frost.z);}
                    if(e.tickCount%40==0&&e.canFreeze()){var owner=level.getServer().getPlayerList().getPlayer(frost.owner);if(owner!=null)secondary(e,level,new DamageSource(owner.damageSources().freeze().typeHolder(),e,owner),1);}
                }
            }
        }
    }
    private static void tickPlayer(ServerPlayer p) {
        var s=profile(p);var a=s.rpgClass()==RpgClass.NONE?ACTIVE.get(p.getUUID()):active(p);long time=now(p);
        if(p.tickCount%20==0)stats(p,s,a);
        if(a==null)return;
        if(p.tickCount%10==0&&(a.statusWasActive||a.until[0]>time||a.until[1]>time||a.until[2]>time))syncStatus(p);
        if(a.ghost&&time>=a.until[1])stopGhost(p,a);
        if(s.rpgClass()==RpgClass.GUERREIRO&&a.until[1]>time&&a.cast[1]!=null&&a.cast[1].mainUnlocked(1)) {
            a.hunger+=(a.cast[1].hasSkill(11)?.9f:1)/6;
            if(a.hunger>=1){int drain=(int)a.hunger;a.hunger-=drain;p.getFoodData().setFoodLevel(Math.max(0,p.getFoodData().getFoodLevel()-drain));}
        }
        if(s.rpgClass()==RpgClass.MAGO) {
            if(a.until[1]>time&&a.cast[1]!=null&&a.cast[1].mainUnlocked(1)&&p.tickCount%10==0)fire(p,a.cast[1]);
            if(a.cast[2]!=null&&a.quakeOrigin!=null) {
                if(p.level()!=a.quakeLevel){clearCracks(p,a);a.quakeOrigin=null;}
                else if(p.tickCount%20==0){if(a.until[2]>time)quake(p,a,false);else if(a.replicas>0){quake(p,a,true);a.replicas--;}else{clearCracks(p,a);a.quakeOrigin=null;}}
            }
        }
        if(s.rpgClass()==RpgClass.ARQUEIRO&&s.hasSkill(8)) {
            if(p.isShiftKeyDown()&&!a.ghost){if(++a.crouchTicks>=40){a.cast[1]=s;startGhost(p,a,40);a.crouchTicks=0;}}
            else if(!p.isShiftKeyDown())a.crouchTicks=0;
        }
        if(a.ghost) {
            var sCast=a.cast[1];boolean full=sCast!=null&&sCast.mainUnlocked(1);
            for(var mob:((ServerLevel)p.level()).getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(32),e->e instanceof Enemy))
                if(mob.getTarget()==p&&(full||p.distanceToSqr(mob)>16))mob.setTarget(null);
            if(sCast!=null&&sCast.mainUnlocked(1)&&p.tickCount%10==0&&a.until[1]-time>(sCast!=null?sCast.abilityDuration(1)-40:0)&&sCast!=null)smoke(p,sCast);
        }
    }
    private static void modifier(ServerPlayer p,net.minecraft.core.Holder<Attribute> attribute,String name,double amount,AttributeModifier.Operation operation) {
        var instance=p.getAttribute(attribute);if(instance==null)return;var id=ResourceLocation.fromNamespaceAndPath("oxyrpg",name);var old=instance.getModifier(id);
        if(old!=null&&old.amount()==amount)return;if(old!=null)instance.removeModifier(id);if(amount!=0)instance.addTransientModifier(new AttributeModifier(id,amount,operation));
    }
    private static void stats(ServerPlayer p,RpgProfile s,Active a) {
        modifier(p,Attributes.MAX_HEALTH,"vitality",s.rpgClass()==RpgClass.GUERREIRO&&s.hasSkill(7)?2:0,AttributeModifier.Operation.ADD_VALUE);
        modifier(p,Attributes.KNOCKBACK_RESISTANCE,"stance",s.hasSkill(14)&&s.rpgClass()!=RpgClass.ARQUEIRO?.1:0,AttributeModifier.Operation.ADD_VALUE);
        double speed=s.rpgClass()==RpgClass.ARQUEIRO&&s.hasSkill(14)?.03:0;
        if(s.rpgClass()==RpgClass.GUERREIRO&&a!=null&&a.until[2]>now(p)&&a.cast[2]!=null&&a.cast[2].hasSkill(19))speed+=.03;
        modifier(p,Attributes.MOVEMENT_SPEED,"agility",speed,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
    }
    public static void damagePre(LivingDamageEvent.Pre event) {
        if(secondaryDamage||Bleeding.damage(event.getSource()))return;
        if(event.getEntity() instanceof ServerPlayer victim){var s=profile(victim);if(s.rpgClass()==RpgClass.GUERREIRO&&s.hasSkill(16))event.setNewDamage(event.getNewDamage()*.95f);
            if(s.rpgClass()==RpgClass.MAGO&&s.hasSkill(0)&&event.getSource().is(DamageTypes.FREEZE))event.setNewDamage(event.getNewDamage()*.9f);}
        if(!attackable(event.getEntity())||!(event.getSource().getEntity() instanceof ServerPlayer p))return;
        var s=profile(p);var a=ACTIVE.get(p.getUUID());float factor=event.getSource().getDirectEntity() instanceof Projectile projectile ? projectile.getPersistentData().getFloatOr("oxy_bonus",1) : 1;boolean arrow=event.getSource().getDirectEntity() instanceof AbstractArrow;
        boolean melee=event.getSource().getDirectEntity()==p;boolean magic=event.getSource().is(DamageTypes.INDIRECT_MAGIC)||event.getSource().is(DamageTypes.MAGIC);
        if(s.rpgClass()==RpgClass.GUERREIRO&&melee){if(s.hasSkill(0))factor*=1.05f;if(a!=null&&a.impetus>now(p))factor*=1.05f;}
        if(s.rpgClass()==RpgClass.MAGO&&magic){if(s.hasSkill(7))factor*=1.05f;if(s.hasSkill(0)&&FROST.containsKey(event.getEntity().getUUID()))factor*=1.1f;}
        if(s.rpgClass()==RpgClass.ARQUEIRO){if(arrow&&s.hasSkill(0))factor*=1.05f;if(a!=null&&a.until[2]>now(p)&&a.cast[2]!=null&&a.cast[2].mainUnlocked(2)&&(melee||arrow))factor*=1.3f+.7f*s.level()/100f+(a.cast[2].hasSkill(19)?.2f*s.level()/100f:0);
            if(a!=null&&a.ghost&&s.hasSkill(13))factor*=1.25f+.5f*s.level()/100f;}
        event.setNewDamage(event.getNewDamage()*factor);
    }
    public static void incoming(LivingIncomingDamageEvent event) {
        if(event.getSource().getDirectEntity() instanceof LivingEntity enemy){var frost=FROST.get(enemy.getUUID());if(frost!=null&&frost.paralyze>enemy.level().getGameTime())event.setCanceled(true);}
    }
    public static void damagePost(LivingDamageEvent.Post event) {
        if(secondaryDamage||Bleeding.damage(event.getSource())||event.getNewDamage()<=0)return;
        var target=event.getEntity();var source=event.getSource();
        if(attackable(target)&&source.getEntity() instanceof ServerPlayer p) {
            var s=profile(p);var a=active(p);long time=now(p);
            if(s.rpgClass()==RpgClass.GUERREIRO&&s.hasSkill(8)) {
                boolean vamp=a.until[1]>time;float heal=vamp?(a.cast[1].mainUnlocked(1)?a.cast[1].hasSkill(13)?.35f:.25f:.1f):s.hasSkill(9)?.08f:.05f;
                if(time>=a.passiveHeal){p.heal(Math.min(event.getNewDamage()*heal,1.5f+2.5f*s.level()/100f));a.passiveHeal=time+(vamp?10:60);if(vamp&&a.cast[1].mainUnlocked(1))p.causeFoodExhaustion(a.cast[1].hasSkill(12)?.6f:.8f); }
                if(vamp&&a.cast[1].mainUnlocked(1)){
                    if(p.tickCount%5==0){sound(p,SoundEvents.GENERIC_EAT.value(),.4f);((ServerLevel)p.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(Items.COOKED_BEEF)),p.getX(),p.getEyeY()-.2,p.getZ(),5,.1,.1,.1,.03);}}
            }
            if(s.rpgClass()==RpgClass.MAGO&&(source.is(DamageTypes.INDIRECT_MAGIC)||source.is(DamageTypes.MAGIC))) {
                if(s.hasSkill(1)&&!FROST.containsKey(target.getUUID()))target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,s.hasSkill(2)?80:60,1));
                if(s.hasSkill(8)&&time>=a.passiveFire){target.igniteForSeconds(s.hasSkill(9)?2:1);a.passiveFire=time+60;}
                if(s.hasSkill(15))for(var e:near((ServerLevel)p.level(),target.position(),s.hasSkill(16)?2:1.5))if(p.hasLineOfSight(e))e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,20,0));
            }
            if(s.rpgClass()==RpgClass.ARQUEIRO) {
                if(source.getDirectEntity() instanceof AbstractArrow&&s.hasSkill(1))target.addEffect(new MobEffectInstance(MobEffects.GLOWING,s.hasSkill(2)?60:40,0));
                boolean back=target.getLookAngle().dot(p.position().subtract(target.position()).normalize())<-.3;
                boolean assassin=a.until[2]>time&&a.cast[2]!=null&&a.cast[2].mainUnlocked(2);
                if((source.getDirectEntity() instanceof AbstractArrow||source.getDirectEntity()==p&&(p.getMainHandItem().is(net.minecraft.tags.ItemTags.SWORDS)||p.getMainHandItem().is(net.minecraft.tags.ItemTags.AXES)))&&((back&&s.hasSkill(15))||assassin)){
                    Bleeding.apply(target,p,assassin&&s.hasSkill(20)?2:assassin||s.hasSkill(16)?1:0);if(assassin)target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,60,0));}
                if(a.ghost){ClassJourney.advance(p,1,1);stopGhost(p,a);}
            }
        }
        if(target instanceof ServerPlayer p&&source.getEntity() instanceof LivingEntity attacker&&attacker instanceof Enemy) {
            var s=profile(p);var a=active(p);boolean bastion=a.until[2]>now(p)&&a.cast[2]!=null&&a.cast[2].mainUnlocked(2);
            if(s.rpgClass()==RpgClass.GUERREIRO&&s.hasSkill(15)&&(bastion||now(p)>=a.passiveThorns)) {
                a.passiveThorns=now(p)+40;float retaliation=bastion?Math.max(1,event.getNewDamage()*.25f)*(s.hasSkill(20)?1.5f:1):1;
                secondary(attacker,(ServerLevel)p.level(),p.damageSources().thorns(p),retaliation);
                if(bastion&&s.hasSkill(20))attacker.knockback(.5,p.getX()-attacker.getX(),p.getZ()-attacker.getZ());
            }
        }
    }
    private static void secondary(LivingEntity e,ServerLevel level,DamageSource source,float amount){boolean old=secondaryDamage;secondaryDamage=true;try{e.hurtServer(level,source,amount);}finally{secondaryDamage=old;}}
    public static void targeting(LivingChangeTargetEvent event) {
        if(!(event.getNewAboutToBeSetTarget() instanceof ServerPlayer p)||!(event.getEntity() instanceof Enemy))return;
        var s=profile(p);var a=ACTIVE.get(p.getUUID());boolean ghost=a!=null&&a.ghost&&a.until[1]>now(p);
        if(ghost&&(a.cast[1]!=null&&a.cast[1].mainUnlocked(1)||p.distanceToSqr(event.getEntity())>16))event.setNewAboutToBeSetTarget(null);
        else if(s.rpgClass()==RpgClass.ARQUEIRO&&s.hasSkill(7)&&event.getEntity() instanceof Mob mob){double range=mob.getAttributeValue(Attributes.FOLLOW_RANGE)*(s.hasSkill(9)?.85:.9);if(p.distanceToSqr(mob)>range*range)event.setNewAboutToBeSetTarget(null);}
    }
    public static void onImpact(ProjectileImpactEvent event) {
        var shot=event.getProjectile();if(!(shot.level() instanceof ServerLevel level)||event.isCanceled())return;
        if(event.getRayTraceResult() instanceof EntityHitResult hit&&hit.getEntity() instanceof ServerPlayer defender) {
            var a=ACTIVE.get(defender.getUUID());if(profile(defender).rpgClass()==RpgClass.GUERREIRO&&a!=null&&a.until[2]>now(defender)&&a.cast[2]!=null&&a.cast[2].mainUnlocked(2)
                &&shot.getDeltaMovement().normalize().dot(defender.getLookAngle())<-.25) {
                shot.deflect(ProjectileDeflection.REVERSE,defender,net.minecraft.world.entity.EntityReference.of(defender),true);shot.setPos(defender.getEyePosition().add(defender.getLookAngle()));
                if(a.cast[2].hasSkill(20)){shot.getPersistentData().putFloat("oxy_bonus",1.5f);}
                event.setCanceled(true);sound(defender,SoundEvents.SHIELD_BLOCK.value(),1);return;
            }
        }
        var tag=shot.getPersistentData();int type=tag.getIntOr("oxy_type",0);if(type==0)return;event.setCanceled(true);
        if(shot.getOwner() instanceof ServerPlayer p) {
            var s=new RpgProfile(RpgClass.values()[tag.getIntOr("oxy_class",1)],100,0,0,0,tag.getLongOr("oxy_skills",0));
            LivingEntity direct=event.getRayTraceResult() instanceof EntityHitResult hit&&hit.getEntity() instanceof LivingEntity e?e:null;
            float bonus=tag.getFloatOr("oxy_bonus",1);var pos=event.getRayTraceResult().getLocation();
            if(type==1) {
                TemporaryBlocks.ice(p,net.minecraft.core.BlockPos.containing(direct==null?pos:direct.getBoundingBox().getCenter()),s.mainUnlocked(0),s.mainUnlocked(0)?s.abilityDuration(0):60);
                double radius=s.mainUnlocked(0)?3+(s.hasSkill(4)?.5:0)+(s.hasSkill(6)?1:0):.6;
                var targets=near(level,pos,radius);
                if(direct!=null&&enemy(direct)&&!targets.contains(direct))targets.add(direct);
                tag.putBoolean("oxy_effect",!targets.isEmpty());
                for(var e:targets)if(p.hasLineOfSight(e)){
                    e.hurtServer(level,p.damageSources().indirectMagic(shot,p),s.abilityDamage()*bonus);
                    if(s.mainUnlocked(0)&&e.canFreeze())FROST.put(e.getUUID(),new Frost(p.getUUID(),level.getGameTime()+s.abilityDuration(0)+ClassEquipment.frost(p),level.getGameTime()+(s.hasSkill(6)?80:60)+ClassEquipment.frost(p),e.getX(),e.getZ()));
                    else e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,(s.hasSkill(2)?80:60)+ClassEquipment.frost(p),1));
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE,pos.x,pos.y,pos.z,30,radius,.4,radius,.06);
            } else if(type==3&&direct!=null&&enemy(direct)){direct.hurtServer(level,p.damageSources().indirectMagic(shot,p),3*bonus);direct.igniteForSeconds(s.hasSkill(9)?2:1);}
            else if(type==2&&direct!=null&&enemy(direct)) {
                String key="hit_"+direct.getUUID();if(!tag.getBooleanOr(key,false)) {
                    tag.putBoolean(key,true);int hits=tag.getIntOr("oxy_hits",0)+1;tag.putInt("oxy_hits",hits);
                    direct.hurtServer(level,p.damageSources().arrow((AbstractArrow)shot,p),s.abilityDamage()*(s.hasSkill(6)?1.25f:1));
                    direct.addEffect(new MobEffectInstance(MobEffects.GLOWING,s.abilityDuration(0),0));
                    if(s.mainUnlocked(0))direct.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,s.hasSkill(6)?120:80,0));
                }
                int limit=s.mainUnlocked(0)?3+(s.hasSkill(4)?1:0):1;
                if(tag.getIntOr("oxy_hits",0)<limit){shot.setPos(pos.add(shot.getDeltaMovement().normalize().scale(direct.getBbWidth()+.5)));return;}
            }
        }
        finishProjectile(shot,event.getRayTraceResult() instanceof EntityHitResult||tag.getBooleanOr("oxy_effect",false));
        var pos=event.getRayTraceResult().getLocation();SkillEffects.impact(level,pos,type==1?2:1,type==1?2:type==2?3:1);level.sendParticles(type==1?ParticleTypes.SNOWFLAKE:type==2?ParticleTypes.END_ROD:ParticleTypes.FLAME,pos.x,pos.y,pos.z,12,.2,.2,.2,.03);shot.discard();
    }
    private static void finishProjectile(Projectile shot,boolean hit) {
        if(!(shot.getOwner() instanceof ServerPlayer p))return;
        var a=ACTIVE.get(p.getUUID());if(a==null)return;
        int slot=shot.getPersistentData().getIntOr("oxy_type",0)==3?1:0;
        if(a.projectile[slot]!=shot.getId())return;
        a.projectile[slot]=0;
        var s=a.cast[slot];
        int duration=!hit||s==null?0:slot==1?(s.hasSkill(9)?40:20):s.mainUnlocked(0)?s.abilityDuration(0):s.rpgClass()==RpgClass.MAGO?(s.hasSkill(2)?80:60):s.abilityDuration(0);
        if(hit&&s!=null&&s.rpgClass()==RpgClass.MAGO&&slot==0)duration+=ClassEquipment.frost(p);
        a.until[slot]=now(p)+duration;a.total[slot]=duration;a.phase[slot]=5;
        syncStatus(p);
    }
    public static void syncStatus(ServerPlayer p) {
        var a=ACTIVE.get(p.getUUID());int[] remaining=new int[3],total=new int[3],phase=new int[3];
        if(a!=null)for(int i=0;i<3;i++){remaining[i]=(int)Math.max(0,a.until[i]-now(p));total[i]=Math.max(1,a.total[i]);phase[i]=remaining[i]>0?a.phase[i]:0;}
        if(profile(p).rpgClass()==RpgClass.NECROMANTE)for(int i=0;i<3;i++){remaining[i]=RpgNecromancer.remaining(p,i);total[i]=i==0?80:Math.max(1,profile(p).abilityDuration(i));phase[i]=remaining[i]!=0?RpgNecromancer.phase(p,i):0;if(phase[i]==1)total[i]=120;}
        if(profile(p).rpgClass()==RpgClass.AVIARIO)for(int i=0;i<3;i++){remaining[i]=RpgAviary.remaining(p,i);total[i]=Math.max(1,profile(p).abilityDuration(i));phase[i]=remaining[i]>0?3:0;}
        if(RpgSpecialists.handles(profile(p).rpgClass()))for(int i=0;i<3;i++){remaining[i]=RpgSpecialists.remaining(p,i);total[i]=Math.max(1,profile(p).abilityDuration(i));phase[i]=remaining[i]>0?3:0;}
        if(a!=null)a.statusWasActive=remaining[0]>0||remaining[1]>0||remaining[2]>0;
        PacketDistributor.sendToPlayer(p,new RpgNetwork.AbilityStatus(remaining[0],remaining[1],remaining[2],total[0],total[1],total[2],phase[0],phase[1],phase[2]));
    }
    public static void cleanse(LivingEntity e){FROST.remove(e.getUUID());e.setTicksFrozen(0);RpgNecromancer.cleanse(e);}
    public static void clear(ServerPlayer p){RpgSpecialists.clear(p);RpgAviary.clear(p);RpgShaman.clear(p);RpgNecromancer.clear(p);var a=ACTIVE.remove(p.getUUID());if(a!=null){stopGhost(p,a);clearCracks(p,a);}stats(p,RpgProfile.NEW,null);}
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){clear(p);PROFILES.remove(p.getUUID());FROST.entrySet().removeIf(x->x.getValue().owner.equals(p.getUUID()));}}
    public static void stop(ServerStoppedEvent e){ACTIVE.clear();PROFILES.clear();FROST.clear();}
}











